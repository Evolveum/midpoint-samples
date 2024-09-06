#!/bin/sh

MIDPOINT_HOME=midpoint_home


cat <<'EOF'
                   _ _____              _
             _    | |  _  \     _     _| |_
   ___ ____ (_) __| | |_) |___ (_)___|_   _|
  |  _ ` _ `| |/ _  |  __/  _ \| |  _` | |
  | | | | | | | (_| | |  | (_) | | | | | |_
  |_| |_| |_|_|\____|_|  \____/|_|_| |_|\__|  by Evolveum and partners

EOF

if [ ! -d "$MIDPOINT_HOME" ]; then
  echo "Creating midPoint home directory: $MIDPOINT_HOME"
  mkdir "$MIDPOINT_HOME"
  if [ $? -ne 0 ]; then
    echo >&2 "Error: Can't create midPoint home directory. Exiting..."
    exit 2
  fi

  chmod g+s "$MIDPOINT_HOME"
  if [ $? -ne 0 ]; then
    echo >&2 "Error: Can't set access rights on midPoint home directory. Exiting..."
    exit 3
  fi
fi

#if [ ! -d "$MIDPOINT_HOME/resources" ]; then
#  echo "Creating resources directory: $MIDPOINT_HOME/resources"
#  mkdir "$MIDPOINT_HOME/resources"
#  if [ $? -ne 0 ]; then
#    echo >&2 "Error: Can't create resources directory. Exiting..."
#    exit 2
#  fi
#fi

#for filename in ./init/midpoint/resources/*; do
#  bfname=$(basename "$filename")
#  echo "Processing $bfname"
#  if [ ! -f "$MIDPOINT_HOME/resources/$bfname" ]; then
#    echo "Initializing $MIDPOINT_HOME/resources/$bfname"
#    cp "$filename" "$MIDPOINT_HOME/resources/$bfname"
#  fi
#done

echo
echo Initialization done. 
echo '========================================================='
echo 'To start midPoint please run: >>>  docker compose up  <<<'
echo '========================================================='
echo Check the midPoint documentation for details.
