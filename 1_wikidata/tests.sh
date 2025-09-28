#/bin/sh
# ------------------------------------------------------------------------------
#
# File: 1_wikidata/tests.sh
# Created: 09/28/25
# Author: Umut Sevdi
# Description: Examine Wikidata (https://www.wikidata.org). Perform some tests 
# with its API.
#
# Project: umutsevdi/swe573-assignment-1
# ------------------------------------------------------------------------------
# SWAGGER API: https://doc.wikimedia.org/Wikibase/master/js/rest-api
# ------------------------------------------------------------------------------
set -e
URL=https://www.wikidata.org/wiki/Special:EntityData/Q42.json
RESTURL=https://www.wikidata.org/w/rest.php/wikibase/v1
RESTURL0=https://www.wikidata.org/w/rest.php/wikibase/v0

TESTNAME()
{
    echo -e "\r\n\033[30;1;2m# $*\033[0m\r\n"
}

# Get Title
TESTNAME Get Title
curl -s "$URL" | jq .entities.Q42.title


# Get All descriptions then filter English and Turkish
TESTNAME Get All descriptions then filter English and Turkish
curl -s --request GET \
  --url "$RESTURL/entities/items/Q42/descriptions" \
  --header 'Content-Type: application/json' | jq -r '.en, .tr'

TESTNAME Get a single language
curl -s --request GET \
  --url "$RESTURL/entities/items/Q42/descriptions/fr" \
  --header 'Content-Type: application/json'

TESTNAME Get aliases
curl -s --request GET \
  --url "$RESTURL/entities/items/Q42/aliases" \
  -H 'Content-Type: application/json' \
  -H 'Accept-Language: en,fr,de'

TESTNAME Search an item
echo "Enter a string to search:"
read _data_
curl -s --request GET \
  --url "$RESTURL0/search/items?q=$_data_&language=en" \
  -H 'Content-Type: application/json' | jq .

