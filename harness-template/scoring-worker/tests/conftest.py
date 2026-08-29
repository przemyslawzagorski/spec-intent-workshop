import pathlib
import sys

KORZEN = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(KORZEN / "src"))

# Polityka lezy w korzeniu repo (return-policy.yaml)
import os
os.environ.setdefault("POLICY_FILE", str(KORZEN.parent / "return-policy.yaml"))
