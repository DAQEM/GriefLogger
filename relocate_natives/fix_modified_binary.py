import sys
import lief
import subprocess
import download_codesign
from pathlib import Path

output_path = sys.argv[1]
binary = lief.parse(sys.stdin.buffer.read())
if binary is None:
    exit(1)

if isinstance(binary, lief.MachO.Binary):
    binary.remove_signature()

binary.write(output_path)

if isinstance(binary, lief.MachO.Binary):
    print(f"Signing {output_path}...")

    if not Path("./apple-codesign/COPYING").exists():
        download_codesign.download_and_unpack()

    sign_process = subprocess.Popen(["./apple-codesign/rcodesign", "sign", output_path], shell=False,
                                    stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    sign_process.wait()