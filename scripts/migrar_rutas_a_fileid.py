#!/usr/bin/env python3
"""
Migra las rutas antiguas "/uploads/..." de la base de datos al fileId de Google Drive.

Requisitos en el servidor: python3, rclone (solo su configuracion, no el montaje) y el
cliente mysql.

Pasos:
  1. rclone lsf <remoto> -R --files-only --format ip  -> todos los archivos con su fileId
  2. SELECT id, ruta FROM <tabla> WHERE ruta LIKE '/uploads/%'
  3. Quita "/uploads/" y busca esa ruta relativa en el listado.
  4. Genera:
       migracion_fileids.sql     UPDATEs en una transaccion
       reversion_fileids.sql     UPDATEs inversos
       sin_archivo_en_drive.txt  filas cuyo archivo no esta en Drive (no se tocan)
  5. Solo aplica con --apply. Si el nombre de la base no contiene "test", exige --force.

IMPORTANTE: aplicar solo cuando el servicio desplegado ya sea la version que lee fileIds.

Ejemplo:
  MYSQL_ARGS="--defaults-extra-file=$HOME/.my_cartera.cnf"
  python3 scripts/migrar_rutas_a_fileid.py --remoto gdrive:archivos/cartera-temporal \\
      --base cartera_temporal --mysql-args "$MYSQL_ARGS"            # simular
  python3 scripts/migrar_rutas_a_fileid.py ... --apply --force       # aplicar
"""
import argparse
import shlex
import subprocess
import sys

PREFIJO = "/uploads/"

# (tabla, columna id, columna ruta)
TABLAS = [
    ("recibo_pago", "id_recibo", "ruta"),
    ("firmas", "id_firma", "ruta"),
]

SALIDA_MIGRACION = "migracion_fileids.sql"
SALIDA_REVERSION = "reversion_fileids.sql"
SALIDA_SIN_ARCHIVO = "sin_archivo_en_drive.txt"


def listar_drive(remoto):
    """Devuelve {ruta relativa: fileId} con una sola llamada a rclone."""
    salida = subprocess.run(
        ["rclone", "lsf", remoto, "-R", "--files-only", "--format", "ip"],
        check=True, capture_output=True, text=True,
    ).stdout
    archivos = {}
    for linea in salida.splitlines():
        if not linea:
            continue
        file_id, _, ruta = linea.partition(";")
        archivos[ruta] = file_id
    return archivos


def mysql(mysql_args, base, sql):
    cmd = ["mysql"] + shlex.split(mysql_args) + ["-N", "-B", base, "-e", sql]
    return subprocess.run(cmd, check=True, capture_output=True, text=True).stdout


def sql_str(valor):
    return "'" + valor.replace("\\", "\\\\").replace("'", "\\'") + "'"


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--remoto", required=True,
                   help="remoto:carpeta de rclone que corresponde a la carpeta antes montada en /uploads")
    p.add_argument("--base", required=True, help="nombre de la base de datos")
    p.add_argument("--mysql-args", default="", help='p.ej. "--defaults-extra-file=$HOME/.my_cartera.cnf"')
    p.add_argument("--apply", action="store_true", help="aplicar los UPDATE (sin esto solo simula)")
    p.add_argument("--force", action="store_true", help="necesario si la base no es de pruebas")
    args = p.parse_args()

    if args.apply and "test" not in args.base.lower() and not args.force:
        sys.exit(f"La base '{args.base}' no parece de pruebas. Usa --force para aplicar.")

    print(f"Listando archivos de {args.remoto} ...")
    drive = listar_drive(args.remoto)
    print(f"  {len(drive)} archivos en Drive")

    updates, reversion, sin_archivo = [], [], []
    for tabla, col_id, col_ruta in TABLAS:
        filas = mysql(args.mysql_args, args.base,
                      f"SELECT `{col_id}`, `{col_ruta}` FROM `{tabla}` WHERE `{col_ruta}` LIKE '{PREFIJO}%'")
        total = 0
        for linea in filas.splitlines():
            if not linea:
                continue
            total += 1
            fila_id, _, ruta = linea.partition("\t")
            file_id = drive.get(ruta[len(PREFIJO):])
            if file_id is None:
                sin_archivo.append(f"{tabla}\t{fila_id}\t{ruta}")
                continue
            updates.append(f"UPDATE `{tabla}` SET `{col_ruta}` = {sql_str(file_id)} "
                           f"WHERE `{col_id}` = {int(fila_id)} AND `{col_ruta}` = {sql_str(ruta)};")
            reversion.append(f"UPDATE `{tabla}` SET `{col_ruta}` = {sql_str(ruta)} "
                             f"WHERE `{col_id}` = {int(fila_id)} AND `{col_ruta}` = {sql_str(file_id)};")
        print(f"  {tabla}: {total} filas con ruta antigua")

    with open(SALIDA_MIGRACION, "w", encoding="utf-8") as f:
        f.write("START TRANSACTION;\n" + "\n".join(updates) + "\nCOMMIT;\n")
    with open(SALIDA_REVERSION, "w", encoding="utf-8") as f:
        f.write("START TRANSACTION;\n" + "\n".join(reversion) + "\nCOMMIT;\n")
    with open(SALIDA_SIN_ARCHIVO, "w", encoding="utf-8") as f:
        f.write("\n".join(sin_archivo) + ("\n" if sin_archivo else ""))

    print(f"A migrar: {len(updates)}  |  Sin archivo en Drive: {len(sin_archivo)}")
    print(f"Generados: {SALIDA_MIGRACION}, {SALIDA_REVERSION}, {SALIDA_SIN_ARCHIVO}")

    if not args.apply:
        print("Simulacion: no se modifico la base. Revisa los archivos y ejecuta con --apply.")
        return

    cmd = ["mysql"] + shlex.split(args.mysql_args) + [args.base]
    with open(SALIDA_MIGRACION, encoding="utf-8") as f:
        subprocess.run(cmd, stdin=f, check=True)
    print("Migracion aplicada. Guarda reversion_fileids.sql y sin_archivo_en_drive.txt con fecha,"
          " fuera de esta carpeta: la siguiente ejecucion los sobrescribe.")


if __name__ == "__main__":
    main()
