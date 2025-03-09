#!/bin/bash

# Set base URL
BASE_URL="http://localhost:8080"

# Set colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Function: Execute request and check response
function make_request() {
    local method=$1
    local endpoint=$2
    local headers=$3
    local data=$4
    local expected_status=$5
    local description=$6

    echo -e "${YELLOW}Test: $description${NC}"
    echo "Request: $method $endpoint"
    
    if [ -n "$data" ]; then
        echo "Data: $data"
    fi
    
    local response
    local status_code
    
    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint" $headers)
    elif [ "$method" == "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" $headers -d "$data")
    elif [ "$method" == "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL$endpoint" $headers)
    fi
    
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    echo "Status code: $status_code"
    echo "Response: $body"
    
    if [ "$status_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ Test passed${NC}"
    else
        echo -e "${RED}✗ Test failed - Expected status code $expected_status, actual status code $status_code${NC}"
    fi
    
    echo "----------------------------------------"
    
    echo "$body"
}

# 1. Login test
echo -e "${YELLOW}Starting API tests...${NC}"
echo "----------------------------------------"

login_response=$(curl -s -X POST "$BASE_URL" /api/auth/login\
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password1"}')

echo "Login response: $login_response"

# Extract token
token=$(echo $login_response | grep -o '"token":"[^"]*' | sed 's/"token":"//')

if [ -z "$token" ]; then
    echo -e "${RED}Login failed, unable to get token${NC}"
    exit 1
else
    echo -e "${GREEN}Successfully obtained token: $token${NC}"
fi

echo "----------------------------------------"

# 2. Validate token
curl -s -X POST "$BASE_URL/api/auth/validate" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

# 3. Get player profile
curl -s -X GET "$BASE_URL/api/player/profile" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

# 4. Get player level
curl -s -X GET "$BASE_URL/api/player/level" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

# 5. Add player experience
curl -s -X POST "$BASE_URL/api/player/experience" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{"experience":20}'
echo -e "\n----------------------------------------"

# 6. Summon monster
summon_response=$(curl -s -X POST "$BASE_URL/api/summon" -H "Authorization: Bearer $token")
echo "Summon response: $summon_response"

# Extract monster ID
monster_id=$(echo $summon_response | grep -o '"id":"[^"]*' | sed 's/"id":"//')

if [ -z "$monster_id" ]; then
    echo -e "${RED}Summon failed, unable to get monster ID${NC}"
    # Continue testing with a fake ID
    monster_id="fake_monster_id"
else
    echo -e "${GREEN}Successfully summoned monster, ID: $monster_id${NC}"
fi

echo "----------------------------------------"

# 7. Get monster details
curl -s -X GET "$BASE_URL/api/monsters/$monster_id" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

# 8. Add monster experience
curl -s -X POST "$BASE_URL/api/monsters/$monster_id/experience" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{"experience":50}'
echo -e "\n----------------------------------------"

# 9. Upgrade monster skill
curl -s -X POST "$BASE_URL/api/monsters/$monster_id/skill" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d '{"skillNum":1}'
echo -e "\n----------------------------------------"

# 10. Summon second monster
summon_response2=$(curl -s -X POST "$BASE_URL/api/summon" -H "Authorization: Bearer $token")
echo "Second monster summon response: $summon_response2"

monster_id2=$(echo $summon_response2 | grep -o '"id":"[^"]*' | sed 's/"id":"//')

if [ -z "$monster_id2" ]; then
    echo -e "${RED}Failed to summon second monster${NC}"
    monster_id2="fake_monster_id_2"
else
    echo -e "${GREEN}Successfully summoned second monster, ID: $monster_id2${NC}"
fi

echo "----------------------------------------"

# 11. Start battle
battle_response=$(curl -s -X POST "$BASE_URL/api/battles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" \
  -d "{\"monster1Id\":\"$monster_id\",\"monster2Id\":\"$monster_id2\"}")

echo "Battle response: $battle_response"

# Extract battle ID
battle_id=$(echo $battle_response | grep -o '"id":"[^"]*' | sed 's/"id":"//')

if [ -z "$battle_id" ]; then
    echo -e "${RED}Battle failed, unable to get battle ID${NC}"
    battle_id="fake_battle_id"
else
    echo -e "${GREEN}Battle successful, ID: $battle_id${NC}"
fi

echo "----------------------------------------"

# 12. Get battle details
curl -s -X GET "$BASE_URL/api/battles/$battle_id" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

# 13. Get monster battle history
curl -s -X GET "$BASE_URL/api/battles/monster/$monster_id" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

# 14. Get summon history
curl -s -X GET "$BASE_URL/api/summon/history" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

# 15. Reprocess failed summons
curl -s -X POST "$BASE_URL/api/summon/reprocess" -H "Authorization: Bearer $token"
echo -e "\n----------------------------------------"

echo -e "${GREEN}Tests completed!${NC}" 