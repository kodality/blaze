#!/bin/sh

client_ready () {
  while true
  do
    # Check that client is ready and feature:install is available
    if echo "feature:install --help; logout" | /opt/karaf/bin/client | grep "Installs a feature with the specified name and version."; then
      echo "Client ready"
      break
    else
      echo "Client not ready yet, waiting..."
      sleep 5
    fi
  done
  return 0
}

client_ready
