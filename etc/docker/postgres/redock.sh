
docker stop ci-postgres-admin && docker rm ci-postgres-admin
`dirname $0`/run-postgres.sh
