# CMPE272 – GitHub Issues Gateway

Service that wraps GitHub Issues REST API for a single repo. Includes CRUD + comments, webhook receiver, OpenAPI 3.1, tests, and Docker.

## Run (local)

```bash
export GITHUB_TOKEN=<fine-grained PAT>
export GITHUB_OWNER=<your user>
export GITHUB_REPO=<your repo>
export WEBHOOK_SECRET=$(openssl rand -hex 32)
export PORT=8080

mvn spring-boot:run
curl -s http://localhost:8080/actuator/health
```

## API Quickstart

Create issue:
```bash
curl -i -X POST http://localhost:$PORT/issues -H 'Content-Type: application/json' -d '{"title":"Demo","body":"hello","labels":["demo"]}'
```

List issues:
```bash
curl -i -G http://localhost:$PORT/issues --data-urlencode state=open --data-urlencode per_page=5
```

Get/Update:
```bash
curl -i http://localhost:$PORT/issues/NUMBER
curl -i -X PATCH http://localhost:$PORT/issues/NUMBER -H 'Content-Type: application/json' -d '{"state":"closed"}'
```

Comment:
```bash
curl -i -X POST http://localhost:$PORT/issues/NUMBER/comments -H 'Content-Type: application/json' -d '{"body":"hi"}'
```

## Webhook

Expose port (ngrok/localtunnel) and configure GitHub webhook with secret `$WEBHOOK_SECRET` for events `issues` and `issue_comment` to `https://<public>/webhook`.  
Debug recent events:
```bash
curl -s http://localhost:$PORT/events?limit=10 | jq .
```

## Docker

```bash
docker build -t issues-gw .
docker run --rm -p 8080:8080   -e PORT=8080   -e GITHUB_TOKEN=$GITHUB_TOKEN   -e GITHUB_OWNER=$GITHUB_OWNER   -e GITHUB_REPO=$GITHUB_REPO   -e WEBHOOK_SECRET=$WEBHOOK_SECRET   issues-gw
```

## Tests & Coverage

```bash
mvn -q test
open target/site/jacoco/index.html
```
