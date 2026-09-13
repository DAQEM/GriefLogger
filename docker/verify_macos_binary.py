import glob
import struct
import sys
import zipfile

MACHO_MAGICS = {
    0xFEEDFACE,
    0xFEEDFACF,
    0xCEFAEDFE,
    0xCFFAEDFE,
    0xCAFEBABE,
    0xBEBAFECA,
    0xCAFEBABF,
    0xBFBAFECA,
}

mod_jars = [
    jar for jar in glob.glob("/app/fabric/build/libs/grieflogger-fabric-*.jar")
    if not jar.endswith("-sources.jar") and not jar.endswith("-dev.jar")
]

if not mod_jars:
    print("Error: No GriefLogger fabric jar found.")
    sys.exit(1)

jar_path = mod_jars[0]
print(f"Checking jar: {jar_path}")

with zipfile.ZipFile(jar_path, "r") as z:
    all_entries = z.namelist()
    mac_entries = [
        name for name in all_entries
        if "Mac" in name and (name.endswith(".dylib") or name.endswith(".jnilib"))
    ]

    if not mac_entries:
        print("Error: No macOS native binaries found in jar.")
        sys.exit(1)

    for entry in mac_entries:
        data = z.read(entry)
        if len(data) < 4:
            print(f"Error: {entry} is too small.")
            sys.exit(1)

        magic = struct.unpack(">I", data[:4])[0]
        if magic not in MACHO_MAGICS:
            print(f"Error: {entry} does not have a valid Mach-O magic header.")
            sys.exit(1)

        if b"gl_sqlite" not in data and b"gl_1sqlite" not in data:
            print(f"Error: {entry} does not contain relocated gl_sqlite symbols.")
            sys.exit(1)

        print(f"Verified macOS binary: {entry} ({len(data)} bytes)")

print("All macOS binaries successfully verified.")
sys.exit(0)