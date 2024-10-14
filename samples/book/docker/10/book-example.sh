#!/bin/sh

MIDPOINT_HOME=midpoint_home

mpinit () {
	
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

  	  if [ ! -d "$MIDPOINT_HOME/resources" ]; then
 	    echo "Creating midPoint home resources directory: $MIDPOINT_HOME/resources"
	    mkdir "$MIDPOINT_HOME/resources"
	    if [ $? -ne 0 ]; then
	      echo >&2 "Error: Can't create midPoint home resources directory. Exiting..."
	      exit 2
	    fi
	  fi

	  chmod g+s "$MIDPOINT_HOME/resources"
	  if [ $? -ne 0 ]; then
	    echo >&2 "Error: Can't set access rights on midPoint home resources directory. Exiting..."
	    exit 3
	  fi

	  if [ ! -d "$MIDPOINT_HOME/post-initial-objects" ]; then
 	    echo "Creating midPoint home post-initial-objects directory: $MIDPOINT_HOME/post-initial-objects"
	    mkdir "$MIDPOINT_HOME/post-initial-objects"
	    if [ $? -ne 0 ]; then
	      echo >&2 "Error: Can't create midPoint home post-initial-objects directory. Exiting..."
	      exit 2
	    fi
	  fi

	  chmod g+s "$MIDPOINT_HOME/post-initial-objects"
	  if [ $? -ne 0 ]; then
	    echo >&2 "Error: Can't set access rights on midPoint home post-initial-objects directory. Exiting..."
	    exit 3
	  fi


	fi

	echo
	echo Initialization done. 
}


mpclean () {
	sudo docker compose down -v
	
	if [ -d "$MIDPOINT_HOME" ]; then
	  rm -rf "$MIDPOINT_HOME"
	  if [ $? -ne 0 ]; then
	    echo >&2 "Error: Can't delete midpoint home directory. Exiting..."
	    exit 4
	  fi
	fi
}

mpreset () {
	mpclean
	mpinit
}

mpup () {
	if [ ! -d "$MIDPOINT_HOME" ]; then
	  mpinit
	fi
	sudo docker compose up
}

case $1 in
    init)
      mpinit
      ;;
    reset)
      mpreset
      ;;
    up)
      mpup
      ;;
    clean)
      mpclean
      ;;
    help)
      mphelp
      ;;
    *)
      mphelp
      ;;
esac

