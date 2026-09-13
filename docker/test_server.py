import glob
import json
import os
import shutil
import subprocess
import sys
import time
import urllib.request

WORK_DIR = "/server"
MODS_DIR = os.path.join(WORK_DIR, "mods")

MC_VERSION = "26.2"
FABRIC_LOADER_VERSION = "0.19.3"
FABRIC_API_VERSION = "0.156.0+26.2"
KNOT_VERSION = "21.2.2"
UILIB_VERSION = "21.1.1"
YAMLCONFIG_VERSION = "21.1.0"

DEPENDENCIES = {
    "fabric-api.jar": f"https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/{FABRIC_API_VERSION}/fabric-api-{FABRIC_API_VERSION}.jar",
    "knot-fabric.jar": f"https://maven.daqem.com/releases/com/daqem/knot/knot-fabric/{KNOT_VERSION}/knot-fabric-{KNOT_VERSION}.jar",
    "uilib-fabric.jar": f"https://maven.daqem.com/releases/com/daqem/uilib/uilib-fabric/{UILIB_VERSION}/uilib-fabric-{UILIB_VERSION}.jar",
    "yamlconfig-fabric.jar": f"https://maven.daqem.com/releases/com/daqem/yamlconfig/yamlconfig-fabric/{YAMLCONFIG_VERSION}/yamlconfig-fabric-{YAMLCONFIG_VERSION}.jar",
}

def download_file(url, destination):
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req) as response, open(destination, "wb") as out_file:
        shutil.copyfileobj(response, out_file)

def get_latest_installer_version():
    req = urllib.request.Request("https://meta.fabricmc.net/v2/versions/installer", headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        return data[0]["version"]

def setup_server():
    os.makedirs(MODS_DIR, exist_ok=True)
    with open(os.path.join(WORK_DIR, "eula.txt"), "w") as f:
        f.write("eula=true\n")

    launch_jar = os.path.join(WORK_DIR, "fabric-server-launch.jar")
    if not os.path.exists(launch_jar):
        installer_version = get_latest_installer_version()
        installer_url = f"https://maven.fabricmc.net/net/fabricmc/fabric-installer/{installer_version}/fabric-installer-{installer_version}.jar"
        installer_jar = os.path.join(WORK_DIR, "fabric-installer.jar")
        download_file(installer_url, installer_jar)

        install_cmd = ["java", "-jar", "fabric-installer.jar", "server", "-mcversion", MC_VERSION, "-loader", FABRIC_LOADER_VERSION, "-downloadMinecraft"]
        subprocess.run(install_cmd, cwd=WORK_DIR, check=True)

    for name, url in DEPENDENCIES.items():
        dest = os.path.join(MODS_DIR, name)
        if not os.path.exists(dest):
            download_file(url, dest)

    mod_jars = [
        jar for jar in glob.glob("/app/fabric/build/libs/grieflogger-fabric-*.jar")
        if not jar.endswith("-sources.jar") and not jar.endswith("-dev.jar")
    ]
    if not mod_jars:
        mod_jars = [
            jar for jar in glob.glob("/build/fabric/build/libs/grieflogger-fabric-*.jar")
            if not jar.endswith("-sources.jar") and not jar.endswith("-dev.jar")
        ]
    if not mod_jars:
        sys.exit(1)
    shutil.copy(mod_jars[0], os.path.join(MODS_DIR, os.path.basename(mod_jars[0])))

def run_server(is_windows):
    if is_windows:
        cmd = ["wine", "/opt/win-jdk/current/bin/java.exe", "-Xmx2048M", "-Xms1024M", "-jar", "fabric-server-launch.jar", "nogui"]
    else:
        cmd = ["java", "-Xmx2048M", "-Xms1024M", "-jar", "fabric-server-launch.jar", "nogui"]

    process = subprocess.Popen(
        cmd,
        cwd=WORK_DIR,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1
    )
    start_time = time.time()
    success = False
    while True:
        line = process.stdout.readline()
        if line:
            sys.stdout.write(line)
            sys.stdout.flush()
            if "Done (" in line or "For help, type \"help\"" in line:
                success = True
                process.stdin.write("stop\n")
                process.stdin.flush()
                break
        if process.poll() is not None:
            break
        if time.time() - start_time > 240:
            process.kill()
            sys.exit(1)
    process.wait()
    if not success or process.returncode != 0:
        sys.exit(1)

if __name__ == "__main__":
    is_win = "--windows" in sys.argv
    setup_server()
    run_server(is_win)