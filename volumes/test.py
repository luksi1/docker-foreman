#!/usr/bin/python3

import subprocess
import re

out = subprocess.check_output("tail -1 foreman/accounts/admin", shell=True)
t = re.sub(r"\\n'", "", out.decode('utf-8'))
o = re.findall("\w{9,}", t)
print(o[0])
