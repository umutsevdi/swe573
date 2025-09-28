# Wiki Data API Data Testing

This directory contains a shell script that test features of 
[Wikidata](https://www.wikidata.org).

Wikidata offers a [Swagger](https://doc.wikimedia.org/Wikibase/master/js/rest-api)
based REST interface that documents all available commands.

The test tool requires `jq` and `curl`.

Test tool can be executed like this:
```sh
chmod +x ./tests.sh
./tests.sh
```

First we set a couple of convenience variables that point at the relevant API 
endpoints:
```sh
# These variables are shorthand for API endpoints.
export URL="https://www.wikidata.org/wiki/Special:EntityData/Q42.json"
export RESTURL="https://www.wikidata.org/w/rest.php/wikibase/v1"
export RESTURL0="https://www.wikidata.org/w/rest.php/wikibase/v0"
```


- Get the JSON and Extract the Title
```sh
curl "$URL" | jq .entities.Q42.title
```

- Pull all descriptions, then filter English and Turkish
```sh
curl --request GET \
  --url "$RESTURL/entities/items/Q42/descriptions" \
  --header 'Content-Type: application/json' | jq -r '.en, .tr'
```

- Retrieve a description in a single language
```sh
curl --request GET \
  --url "$RESTURL/entities/items/Q42/descriptions/fr" \
  --header 'Content-Type: application/json'
```

- List all aliases for the entity
```sh
curl -s --request GET \
  --url "$RESTURL/entities/items/Q42/aliases" \
  -H 'Content-Type: application/json'
```

- Search for an Item
```sh
echo "Enter a string to search:"
read _data_
curl -s --request GET \
  --url "$RESTURL0/search/items?q=$_data_&language=en" \
  -H 'Content-Type: application/json' | jq .
```
