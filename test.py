#!/usr/bin/env python3
"""Quick push test — run: python3 test.py"""

import subprocess
import sys
from datetime import datetime, timezone


def run(cmd: list[str]) -> tuple[int, str]:
    result = subprocess.run(cmd, capture_output=True, text=True)
    out = (result.stdout + result.stderr).strip()
    return result.returncode, out


def main() -> int:
    print("=== Git Push Test ===")

    code, out = run(["git", "remote", "-v"])
    if code != 0:
        print("FAIL: not a git repo")
        print(out)
        return 1
    print("Remote:")
    print(out or "(none)")

    code, branch = run(["git", "branch", "--show-current"])
    branch = branch.strip() or "main"

    code, out = run(["git", "status", "--short"])
    print(f"\nBranch: {branch}")
    print("Status:", out or "clean")

    marker = f"# push test {datetime.now(timezone.utc).isoformat()}\n"
    with open(__file__, "a", encoding="utf-8") as f:
        if marker.strip() not in open(__file__, encoding="utf-8").read():
            f.write("\n" + marker)

    for cmd in [
        ["git", "add", "test.py"],
        ["git", "commit", "-m", f"test: verify push access ({branch})"],
        ["git", "push", "-u", "origin", branch],
    ]:
        code, out = run(cmd)
        print(f"\n$ {' '.join(cmd)}")
        print(out or "(ok)")
        if code != 0 and "push" in cmd:
            print("\nPush failed — check GitHub access / branch name.")
            return code

    print("\nSUCCESS: push completed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

# push test 2026-06-11T01:55:07.365569+00:00
