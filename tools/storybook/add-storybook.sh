#!/usr/bin/env bash
# Adds the Storybook to the web build, at /storybook/: the same bundle, which starts the Storybook from that folder.
# KorGE reads the resources relative to the page URL, ignoring a <base>, so the files of the game are copied instead of
# shared. Run by the Deploy JS workflow and the storybook skill, from the repository root, after
# ./gradlew browserReleaseWebpack. See docs/architecture/storybook.md.
set -euo pipefail

www="${1:-build/www}"
if [ ! -f "$www/index.html" ]; then
  echo "The web build is missing ($www/index.html). Build it first: ./gradlew browserReleaseWebpack" >&2
  exit 1
fi
mkdir -p "$www/storybook"
find "$www" -maxdepth 1 -type f -exec cp {} "$www/storybook/" \;
test -f "$www/storybook/resources.zip"
grep -q 'hello-world.js' "$www/storybook/index.html"
echo "Storybook added at $www/storybook/"
