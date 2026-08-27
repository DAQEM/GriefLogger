import sys
import lief
import subprocess
import download_codesign
from pathlib import Path

output_path = sys.argv[1]
raw_data = sys.stdin.buffer.read()

# 1. Write the raw modified data directly to disk.
# Because we used a 10-character replacement ("grieflogdb") for a 10-character
# target ("org/sqlite"), the binary's internal offsets are perfectly intact.
# We bypass LIEF's builder for ELF (.so) and PE (.dll) files to prevent corruption.
with open(output_path, "wb") as f:
    f.write(raw_data)

# 2. Parse the binary with LIEF to check if it's a macOS (Mach-O) binary.
binary = lief.parse(raw_data)
if binary is None:
    sys.exit(0)

# 3. If it is a Mach-O binary, we must remove the invalid signature and re-sign it.
if isinstance(binary, lief.MachO.Binary):
    print(f"Rebuilding Mach-O binary to remove old signature: {output_path}")
    binary.remove_signature()
    binary.write(output_path)

    print(f"Signing {output_path}...")
    if not Path("./apple-codesign/COPYING").exists():
        download_codesign.download_and_unpack()

    sign_process = subprocess.Popen(["./apple-codesign/rcodesign", "sign", output_path], shell=False,
                                    stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    sign_process.wait()

    if sign_process.returncode != 0:
        sys.exit(sign_process.returncode)