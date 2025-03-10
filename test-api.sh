#!/bin/bash

# Color definitions
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No color

# API base URL
API_URL="http://localhost/api"

# Test result counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# Default timeout in seconds
TIMEOUT=10

# Log file
LOG_FILE="api_test_$(date +%Y%m%d_%H%M%S).log"

# Test groups to run (default: all)
TEST_GROUPS="all"

# Language (default: en)
LANGUAGE="en"

# Help message
show_help() {
    echo -e "${BOLD}Usage:${NC} $0 [options]"
    echo ""
    echo "Options:"
    echo "  -h, --help                 Show this help message"
    echo "  -u, --url URL              Set API base URL (default: http://localhost/api)"
    echo "  -t, --timeout SECONDS      Set request timeout in seconds (default: 10)"
    echo "  -g, --groups GROUPS        Specify test groups to run (comma-separated)"
    echo "                             Available groups: auth, player, monster, summon, battle, royal"
    echo "                             Example: -g auth,player,battle"
    echo "  -l, --log FILE             Set log file (default: api_test_YYYYMMDD_HHMMSS.log)"
    echo "  -v, --verbose              Enable verbose output"
    echo "  -c, --chinese              Display messages in Chinese"
    echo "  -m, --manual               Run manual test"
    echo "  -s, --simple               Run simple tests"
    echo ""
    exit 0
}

# Log function
log() {
    echo "$1" >> "$LOG_FILE"
    if [ "$VERBOSE" = true ]; then
        echo "$1"
    fi
}

# Check if a test group should be run
should_run_group() {
    local group=$1
    if [ "$TEST_GROUPS" = "all" ]; then
        return 0
    else
        if [[ "$TEST_GROUPS" == *"$group"* ]]; then
            return 0
        else
            return 1
        fi
    fi
}

# Test function with improved error handling and timeout
run_test() {
    local test_name=$1
    local command=$2
    local expected_status=$3
    local validation_check=$4
    
    echo -e "${BLUE}${MSG_TEST}: ${test_name}${NC}"
    log "${MSG_TEST}: $test_name"
    log "Command: $command"
    
    # Execute command with timeout and get HTTP status code
    local temp_file=$(mktemp)
    local start_time=$(date +%s)
    
    # Add timeout to curl command
    if [[ "$command" == *"curl"* ]]; then
        command="${command/curl/curl --max-time $TIMEOUT}"
    fi
    
    # Execute command
    eval $command > "$temp_file" 2>&1
    local status=$?
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    # Read response
    local response=$(cat "$temp_file")
    rm "$temp_file"
    
    # Increment test counter
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Check status code
    if [ $status -eq $expected_status ]; then
        # If validation check is provided, run it
        if [ -n "$validation_check" ]; then
            eval "$validation_check \"$response\""
            local validation_status=$?
            if [ $validation_status -eq 0 ]; then
                echo -e "${GREEN}${MSG_PASSED}: ${MSG_TEST} $status, ${MSG_PASSED}${NC}"
                log "${MSG_PASSED}: ${MSG_TEST} $status, ${MSG_PASSED}"
                PASSED_TESTS=$((PASSED_TESTS + 1))
            else
                echo -e "${RED}${MSG_FAILED}: ${MSG_TEST} $status, ${MSG_FAILED}${NC}"
                log "${MSG_FAILED}: ${MSG_TEST} $status, ${MSG_FAILED}"
                FAILED_TESTS=$((FAILED_TESTS + 1))
            fi
        else
            echo -e "${GREEN}${MSG_PASSED}: ${MSG_TEST} $status${NC}"
            log "${MSG_PASSED}: ${MSG_TEST} $status"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        fi
    else
        echo -e "${RED}${MSG_FAILED}: ${MSG_TEST} $status, ${MSG_PASSED} $expected_status${NC}"
        log "${MSG_FAILED}: ${MSG_TEST} $status, ${MSG_PASSED} $expected_status"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    # Display response (truncate if too long)
    if [ ${#response} -gt 500 ]; then
        echo "${MSG_RESPONSE} (${MSG_TRUNCATED}): ${response:0:500}..."
        log "${MSG_RESPONSE} (${MSG_TRUNCATED}): ${response:0:500}..."
    else
        echo "${MSG_RESPONSE}: $response"
        log "${MSG_RESPONSE}: $response"
    fi
    
    echo -e "${CYAN}${MSG_DURATION}: ${duration}s${NC}"
    log "${MSG_DURATION}: ${duration}s"
    echo ""
    log ""
    
    # Return response for further processing
    echo "$response"
}

# Skip test function
skip_test() {
    local test_name=$1
    local reason=$2
    
    echo -e "${YELLOW}${MSG_SKIPPED}: ${test_name}${NC}"
    echo -e "${YELLOW}${MSG_REASON}: ${reason}${NC}"
    log "${MSG_SKIPPED}: $test_name"
    log "${MSG_REASON}: $reason"
    echo ""
    log ""
    
    SKIPPED_TESTS=$((SKIPPED_TESTS + 1))
}

# Print test header
print_header() {
    echo -e "${YELLOW}======================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}======================================${NC}"
    echo ""
    
    log "======================================"
    log "$1"
    log "======================================"
    log ""
}

# JSON validation functions
validate_token() {
    local response=$1
    if [[ "$response" == *"token"* ]]; then
        return 0
    else
        return 1
    fi
}

validate_user() {
    local response=$1
    if [[ "$response" == *"username"* ]]; then
        return 0
    else
        return 1
    fi
}

validate_monster() {
    local response=$1
    if [[ "$response" == *"id"* && "$response" == *"element"* ]]; then
        return 0
    else
        return 1
    fi
}

validate_battle() {
    local response=$1
    if [[ "$response" == *"id"* && "$response" == *"winnerId"* ]]; then
        return 0
    else
        return 1
    fi
}

validate_rumble() {
    local response=$1
    if [[ "$response" == *"id"* && "$response" == *"participantIds"* ]]; then
        return 0
    else
        return 1
    fi
}

# Extract JSON value function
extract_json_value() {
    local json=$1
    local key=$2
    
    # Try using jq if available
    if command -v jq &> /dev/null; then
        local value=$(echo "$json" | jq -r ".$key" 2>/dev/null)
        if [ "$value" != "null" ] && [ -n "$value" ]; then
            echo "$value"
            return
        fi
    fi
    
    # Fallback to grep/sed
    local value=$(echo "$json" | grep -o "\"$key\":\"[^\"]*\"" | sed "s/\"$key\":\"//;s/\"//")
    echo "$value"
}

# Get a fresh token
get_token() {
    echo "Getting a fresh token..."
    local response=$(curl -s -X POST $API_URL/auth/login -H 'Content-Type: application/json' -d '{"username": "user1", "password": "password1"}')
    local token=$(echo "$response" | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//g')
    
    if [ -n "$token" ]; then
        echo -e "${BLUE}${MSG_RETRIEVED} ${MSG_TOKEN}: $token${NC}"
        log "${MSG_RETRIEVED} ${MSG_TOKEN}: $token"
        echo ""
        log ""
        echo "$token"
    else
        echo -e "${RED}${MSG_NO_TOKEN}${NC}"
        log "${MSG_NO_TOKEN}"
        echo ""
    fi
}

# Manual test function
manual_test() {
    echo -e "${BOLD}Running manual test...${NC}"
    
    # Get token
    echo "Getting a fresh token..."
    local response=$(curl -s -X POST $API_URL/auth/login -H 'Content-Type: application/json' -d '{"username": "user1", "password": "password1"}')
    echo "Login response: $response"
    local token=$(echo "$response" | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//g')
    
    if [ -z "$token" ]; then
        echo -e "${RED}Failed to get token, cannot proceed with manual test${NC}"
        return 1
    fi
    
    echo -e "${BLUE}Retrieved token: $token${NC}"
    
    # Test player profile
    echo -e "${BLUE}Testing player profile...${NC}"
    curl -v -X GET "$API_URL/player/profile" -H "Authorization: Bearer $token"
    echo ""
    
    # Test player monsters
    echo -e "${BLUE}Testing player monsters...${NC}"
    curl -v -X GET "$API_URL/player/monsters" -H "Authorization: Bearer $token"
    echo ""
    
    echo -e "${GREEN}Manual test completed${NC}"
}

# Simple test function
simple_test() {
    echo -e "${BOLD}Running simple API tests...${NC}"
    
    # Get token
    echo -e "${BLUE}Getting token...${NC}"
    TOKEN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" -H "Content-Type: application/json" -d '{"username": "user1", "password": "password1"}')
    echo "Token response: $TOKEN_RESPONSE"
    TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//g')
    
    if [ -z "$TOKEN" ]; then
        echo -e "${RED}Failed to get token, cannot proceed with tests${NC}"
        return 1
    fi
    
    echo -e "${GREEN}Retrieved token: $TOKEN${NC}"
    
    # Test player profile
    echo -e "${BLUE}Testing player profile...${NC}"
    PROFILE_RESPONSE=$(curl -s -X GET "$API_URL/player/profile" -H "Authorization: Bearer $TOKEN")
    echo "Profile response: $PROFILE_RESPONSE"
    
    # Test player monsters
    echo -e "${BLUE}Testing player monsters...${NC}"
    MONSTERS_RESPONSE=$(curl -s -X GET "$API_URL/player/monsters" -H "Authorization: Bearer $TOKEN")
    echo "Monsters response: $MONSTERS_RESPONSE"
    
    # Extract first monster ID
    MONSTER_ID=$(echo "$MONSTERS_RESPONSE" | grep -o '"[0-9a-f]\{24\}"' | head -1 | sed 's/"//g')
    if [ -z "$MONSTER_ID" ]; then
        echo -e "${YELLOW}Warning: No monster ID found${NC}"
    else
        echo -e "${GREEN}Retrieved monster ID: $MONSTER_ID${NC}"
        
        # Test monster details
        echo -e "${BLUE}Testing monster details...${NC}"
        MONSTER_RESPONSE=$(curl -s -X GET "$API_URL/monsters/$MONSTER_ID" -H "Authorization: Bearer $TOKEN")
        echo "Monster response: $MONSTER_RESPONSE"
        
        # Test add experience to monster
        echo -e "${BLUE}Testing add experience to monster...${NC}"
        EXP_RESPONSE=$(curl -s -X POST "$API_URL/monsters/$MONSTER_ID/experience" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"experience": 50}')
        echo "Experience response: $EXP_RESPONSE"
        
        # Test upgrade monster skill
        echo -e "${BLUE}Testing upgrade monster skill...${NC}"
        SKILL_RESPONSE=$(curl -s -X POST "$API_URL/monsters/$MONSTER_ID/skill" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"skillNum": 1}')
        echo "Skill upgrade response: $SKILL_RESPONSE"
    fi
    
    # Test summon
    echo -e "${BLUE}Testing summon...${NC}"
    SUMMON_RESPONSE=$(curl -s -X POST "$API_URL/summon" -H "Authorization: Bearer $TOKEN")
    echo "Summon response: $SUMMON_RESPONSE"
    
    # Test summon history
    echo -e "${BLUE}Testing summon history...${NC}"
    SUMMON_HISTORY=$(curl -s -X GET "$API_URL/summon/history" -H "Authorization: Bearer $TOKEN")
    echo "Summon history: $SUMMON_HISTORY"
    
    # Test battle
    if [ -n "$MONSTER_ID" ]; then
        # Extract second monster ID
        MONSTER_ID2=$(echo "$MONSTERS_RESPONSE" | grep -o '"[0-9a-f]\{24\}"' | sed 's/"//g' | sed -n '2p')
        if [ -n "$MONSTER_ID2" ]; then
            echo -e "${GREEN}Retrieved second monster ID: $MONSTER_ID2${NC}"
            
            echo -e "${BLUE}Testing battle...${NC}"
            BATTLE_RESPONSE=$(curl -s -X POST "$API_URL/battles" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"monster1Id\": \"$MONSTER_ID\", \"monster2Id\": \"$MONSTER_ID2\"}")
            echo "Battle response: $BATTLE_RESPONSE"
            
            # Extract battle ID
            BATTLE_ID=$(echo "$BATTLE_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | sed 's/"id":"//;s/"//g')
            if [ -n "$BATTLE_ID" ]; then
                echo -e "${GREEN}Retrieved battle ID: $BATTLE_ID${NC}"
                
                # Test get battle details
                echo -e "${BLUE}Testing get battle details...${NC}"
                BATTLE_DETAILS=$(curl -s -X GET "$API_URL/battles/$BATTLE_ID" -H "Authorization: Bearer $TOKEN")
                echo "Battle details: $BATTLE_DETAILS"
            fi
            
            # Test get monster battle history
            echo -e "${BLUE}Testing get monster battle history...${NC}"
            MONSTER_BATTLES=$(curl -s -X GET "$API_URL/battles/monster/$MONSTER_ID" -H "Authorization: Bearer $TOKEN")
            echo "Monster battles: $MONSTER_BATTLES"
            
            # Test get all battle history
            echo -e "${BLUE}Testing get all battle history...${NC}"
            ALL_BATTLES=$(curl -s -X GET "$API_URL/battles/history" -H "Authorization: Bearer $TOKEN")
            echo "All battles: $ALL_BATTLES"
            
            # Test royal rumble if we have at least 3 monsters
            MONSTER_ID3=$(echo "$MONSTERS_RESPONSE" | grep -o '"[0-9a-f]\{24\}"' | sed 's/"//g' | sed -n '3p')
            if [ -n "$MONSTER_ID3" ]; then
                echo -e "${GREEN}Retrieved third monster ID: $MONSTER_ID3${NC}"
                
                echo -e "${BLUE}Testing royal rumble...${NC}"
                RUMBLE_RESPONSE=$(curl -s -X POST "$API_URL/royal-rumble" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"monsterIds\": [\"$MONSTER_ID\", \"$MONSTER_ID2\", \"$MONSTER_ID3\"]}")
                echo "Royal rumble response: $RUMBLE_RESPONSE"
                
                # Extract rumble ID
                RUMBLE_ID=$(echo "$RUMBLE_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | sed 's/"id":"//;s/"//g')
                if [ -n "$RUMBLE_ID" ]; then
                    echo -e "${GREEN}Retrieved rumble ID: $RUMBLE_ID${NC}"
                    
                    # Test get rumble experience
                    echo -e "${BLUE}Testing get rumble experience...${NC}"
                    RUMBLE_EXP=$(curl -s -X GET "$API_URL/royal-rumble/experience/$RUMBLE_ID" -H "Authorization: Bearer $TOKEN")
                    echo "Rumble experience: $RUMBLE_EXP"
                fi
                
                # Test get all royal rumble history
                echo -e "${BLUE}Testing get all royal rumble history...${NC}"
                ALL_RUMBLES=$(curl -s -X GET "$API_URL/royal-rumble" -H "Authorization: Bearer $TOKEN")
                echo "All rumbles: $ALL_RUMBLES"
            else
                echo -e "${YELLOW}Warning: Not enough monsters for royal rumble test${NC}"
            fi
        else
            echo -e "${YELLOW}Warning: Not enough monsters for battle test${NC}"
        fi
    fi
    
    echo -e "${GREEN}Simple tests completed${NC}"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -u|--url)
            API_URL="$2"
            shift 2
            ;;
        -t|--timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        -g|--groups)
            TEST_GROUPS="$2"
            shift 2
            ;;
        -l|--log)
            LOG_FILE="$2"
            shift 2
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -c|--chinese)
            LANGUAGE="zh"
            shift
            ;;
        -m|--manual)
            manual_test
            exit $?
            ;;
        -s|--simple)
            simple_test
            exit $?
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            ;;
    esac
done

# Language-specific messages
if [ "$LANGUAGE" = "zh" ]; then
    MSG_STARTING="开始API测试"
    MSG_API_URL="API地址"
    MSG_LOG_FILE="日志文件"
    MSG_TEST="测试"
    MSG_PASSED="通过"
    MSG_FAILED="失败"
    MSG_SKIPPED="跳过"
    MSG_REASON="原因"
    MSG_RESPONSE="响应"
    MSG_TRUNCATED="截断"
    MSG_DURATION="持续时间"
    MSG_WARNING="警告"
    MSG_RETRIEVED="获取到"
    MSG_TOKEN="令牌"
    MSG_MONSTER_ID="怪物ID"
    MSG_BATTLE_ID="战斗ID"
    MSG_RUMBLE_ID="皇家大乱斗ID"
    MSG_TOTAL_TESTS="测试总数"
    MSG_PASSED_TESTS="通过测试"
    MSG_FAILED_TESTS="失败测试"
    MSG_SKIPPED_TESTS="跳过测试"
    MSG_PASS_RATE="通过率"
    MSG_NO_TESTS="没有执行测试"
    MSG_ALL_PASSED="所有测试通过！"
    MSG_SOME_FAILED="部分测试失败！"
    MSG_LOG_SAVED="日志文件保存至"
    MSG_TEST_SUMMARY="测试结果摘要"
    MSG_AUTH_TESTS="1. 认证API测试"
    MSG_PLAYER_TESTS="2. 玩家API测试"
    MSG_MONSTER_TESTS="3. 怪物API测试"
    MSG_SUMMON_TESTS="4. 召唤API测试"
    MSG_BATTLE_TESTS="5. 战斗API测试"
    MSG_ROYAL_TESTS="6. 皇家大乱斗API测试"
    MSG_GROUP_NOT_SELECTED="测试组未选择"
    MSG_NO_TOKEN="无法获取令牌，跳过剩余测试"
    MSG_NO_MONSTER_ID="未找到怪物ID，将使用默认ID进行测试"
    MSG_NO_SECOND_MONSTER="未找到第二个怪物ID，将使用默认ID进行测试"
    MSG_NO_THIRD_MONSTER="未找到第三个怪物ID，将使用默认ID进行测试"
    MSG_NO_BATTLE_ID="未找到战斗ID，将使用默认ID进行测试"
    MSG_NO_RUMBLE_ID="未找到皇家大乱斗ID，将使用默认ID进行测试"
else
    MSG_STARTING="Starting API Tests"
    MSG_API_URL="API URL"
    MSG_LOG_FILE="Log File"
    MSG_TEST="Test"
    MSG_PASSED="Passed"
    MSG_FAILED="Failed"
    MSG_SKIPPED="Skipped"
    MSG_REASON="Reason"
    MSG_RESPONSE="Response"
    MSG_TRUNCATED="truncated"
    MSG_DURATION="Duration"
    MSG_WARNING="Warning"
    MSG_RETRIEVED="Retrieved"
    MSG_TOKEN="Token"
    MSG_MONSTER_ID="Monster ID"
    MSG_BATTLE_ID="Battle ID"
    MSG_RUMBLE_ID="Royal Rumble ID"
    MSG_TOTAL_TESTS="Total Tests"
    MSG_PASSED_TESTS="Passed Tests"
    MSG_FAILED_TESTS="Failed Tests"
    MSG_SKIPPED_TESTS="Skipped Tests"
    MSG_PASS_RATE="Pass Rate"
    MSG_NO_TESTS="No tests were executed"
    MSG_ALL_PASSED="All tests passed!"
    MSG_SOME_FAILED="Some tests failed!"
    MSG_LOG_SAVED="Log file saved to"
    MSG_TEST_SUMMARY="Test Result Summary"
    MSG_AUTH_TESTS="1. Authentication API Tests"
    MSG_PLAYER_TESTS="2. Player API Tests"
    MSG_MONSTER_TESTS="3. Monster API Tests"
    MSG_SUMMON_TESTS="4. Summon API Tests"
    MSG_BATTLE_TESTS="5. Battle API Tests"
    MSG_ROYAL_TESTS="6. Royal Rumble API Tests"
    MSG_GROUP_NOT_SELECTED="Test group not selected"
    MSG_NO_TOKEN="Failed to retrieve token, skipping remaining tests"
    MSG_NO_MONSTER_ID="No monster ID found, will use default ID for testing"
    MSG_NO_SECOND_MONSTER="No second monster ID found, will use default ID for testing"
    MSG_NO_THIRD_MONSTER="No third monster ID found, will use default ID for testing"
    MSG_NO_BATTLE_ID="No battle ID found, will use default ID for testing"
    MSG_NO_RUMBLE_ID="No royal rumble ID found, will use default ID for testing"
fi

# Initialize log file
echo "API Test Log - $(date)" > "$LOG_FILE"
echo "$MSG_API_URL: $API_URL" >> "$LOG_FILE"
echo "Test Groups: $TEST_GROUPS" >> "$LOG_FILE"
echo "Language: $LANGUAGE" >> "$LOG_FILE"
echo "----------------------------------------" >> "$LOG_FILE"

# Main test execution
echo -e "${BOLD}${MSG_STARTING}${NC}"
echo -e "${BLUE}${MSG_API_URL}: ${API_URL}${NC}"
echo -e "${BLUE}${MSG_LOG_FILE}: ${LOG_FILE}${NC}"
echo ""

# Get a token for all tests
TOKEN=$(get_token)
if [ -z "$TOKEN" ]; then
    TOKEN="dXNlcjEtMjAyNS8wMy8xMC0yMDowMDowMA=="
    echo -e "${YELLOW}${MSG_WARNING}: Using default token${NC}"
fi

# 1. Authentication API Tests
if should_run_group "auth"; then
    print_header "$MSG_AUTH_TESTS"

    # 1.1 Validate Token by getting user profile
    run_test "Validate Token" "curl -s -X GET '$API_URL/player/profile' -H 'Authorization: Bearer $TOKEN'" 0 "validate_user"
else
    skip_test "$MSG_AUTH_TESTS" "$MSG_GROUP_NOT_SELECTED"
fi

# 2. Player API Tests
if should_run_group "player"; then
    print_header "$MSG_PLAYER_TESTS"

    # 2.1 Get Player Profile
    run_test "Get Player Profile" "curl -s -X GET '$API_URL/player/profile' -H 'Authorization: Bearer $TOKEN'" 0 "validate_user"

    # 2.2 Get Player Monsters List
    player_monsters_response=$(run_test "Get Player Monsters List" "curl -s -X GET '$API_URL/player/monsters' -H 'Authorization: Bearer $TOKEN'" 0)

    # Extract first monster ID for subsequent tests
    # Try to use jq if available, otherwise fallback to grep/sed
    if command -v jq &> /dev/null; then
        MONSTER_ID=$(echo "$player_monsters_response" | jq -r '.[0]' 2>/dev/null)
    else
        MONSTER_ID=$(echo "$player_monsters_response" | grep -o '"[0-9a-f]\{24\}"' | head -1 | sed 's/"//g')
    fi
    
    if [ -z "$MONSTER_ID" ] || [ "$MONSTER_ID" = "null" ]; then
        echo -e "${YELLOW}${MSG_WARNING}: ${MSG_NO_MONSTER_ID}${NC}"
        log "${MSG_WARNING}: ${MSG_NO_MONSTER_ID}"
        MONSTER_ID="000000000000000000000000"
    else
        echo -e "${BLUE}${MSG_RETRIEVED} ${MSG_MONSTER_ID}: $MONSTER_ID${NC}"
        log "${MSG_RETRIEVED} ${MSG_MONSTER_ID}: $MONSTER_ID"
    fi
    echo ""
    log ""

    # 2.3 Get Player Level
    run_test "Get Player Level" "curl -s -X GET '$API_URL/player/level' -H 'Authorization: Bearer $TOKEN'" 0

    # 2.4 Add Player Experience
    run_test "Add Player Experience" "curl -s -X POST '$API_URL/player/experience' -H 'Authorization: Bearer $TOKEN' -H 'Content-Type: application/json' -d '{\"experience\": 20}'" 0

    # 2.5 Player Level Up
    run_test "Player Level Up" "curl -s -X POST '$API_URL/player/levelup' -H 'Authorization: Bearer $TOKEN'" 0
else
    skip_test "$MSG_PLAYER_TESTS" "$MSG_GROUP_NOT_SELECTED"
fi

# 3. Monster API Tests
if should_run_group "monster"; then
    print_header "$MSG_MONSTER_TESTS"

    # 3.1 Get All Monsters
    all_monsters_response=$(run_test "Get All Monsters" "curl -s -X GET \"$API_URL/monsters\" -H \"Authorization: Bearer $TOKEN\"" 0)

    # 3.2 Get Specific Monster Details
    run_test "Get Specific Monster Details" "curl -s -X GET \"$API_URL/monsters/$MONSTER_ID\" -H \"Authorization: Bearer $TOKEN\"" 0 "validate_monster"

    # 3.3 Add Experience to Monster
    run_test "Add Experience to Monster" "curl -s -X POST \"$API_URL/monsters/$MONSTER_ID/experience\" -H \"Authorization: Bearer $TOKEN\" -H \"Content-Type: application/json\" -d '{\"experience\": 50}'" 0 "validate_monster"

    # 3.4 Upgrade Monster Skill
    run_test "Upgrade Monster Skill" "curl -s -X POST \"$API_URL/monsters/$MONSTER_ID/skill\" -H \"Authorization: Bearer $TOKEN\" -H \"Content-Type: application/json\" -d '{\"skillNum\": 1}'" 0 "validate_monster"
else
    skip_test "$MSG_MONSTER_TESTS" "$MSG_GROUP_NOT_SELECTED"
fi

# 4. Summon API Tests
if should_run_group "summon"; then
    print_header "$MSG_SUMMON_TESTS"

    # 4.1 Summon New Monster
    summon_response=$(run_test "Summon New Monster" "curl -s -X POST \"$API_URL/summon\" -H \"Authorization: Bearer $TOKEN\"" 0 "validate_monster")

    # 4.2 Get Summon History
    run_test "Get Summon History" "curl -s -X GET \"$API_URL/summon/history\" -H \"Authorization: Bearer $TOKEN\"" 0
    
    # 4.3 Reprocess Failed Summons
    run_test "Reprocess Failed Summons" "curl -s -X POST \"$API_URL/summon/reprocess\" -H \"Authorization: Bearer $TOKEN\"" 0
else
    skip_test "$MSG_SUMMON_TESTS" "$MSG_GROUP_NOT_SELECTED"
fi

# 5. Battle API Tests
if should_run_group "battle"; then
    print_header "$MSG_BATTLE_TESTS"

    # Get second monster ID for battle testing
    if command -v jq &> /dev/null; then
        MONSTER_ID2=$(echo "$player_monsters_response" | jq -r '.[1]' 2>/dev/null)
    else
        MONSTER_ID2=$(echo "$player_monsters_response" | grep -o '"[0-9a-f]\{24\}"' | sed 's/"//g' | sed -n '2p')
    fi
    
    if [ -z "$MONSTER_ID2" ] || [ "$MONSTER_ID2" = "null" ]; then
        echo -e "${YELLOW}${MSG_WARNING}: ${MSG_NO_SECOND_MONSTER}${NC}"
        log "${MSG_WARNING}: ${MSG_NO_SECOND_MONSTER}"
        MONSTER_ID2="000000000000000000000001"
    else
        echo -e "${BLUE}${MSG_RETRIEVED} ${MSG_MONSTER_ID} 2: $MONSTER_ID2${NC}"
        log "${MSG_RETRIEVED} ${MSG_MONSTER_ID} 2: $MONSTER_ID2"
    fi
    echo ""
    log ""

    # 5.1 Conduct Battle
    battle_response=$(run_test "Conduct Battle" "curl -s -X POST \"$API_URL/battles\" -H \"Authorization: Bearer $TOKEN\" -H \"Content-Type: application/json\" -d '{\"monster1Id\": \"$MONSTER_ID\", \"monster2Id\": \"$MONSTER_ID2\"}'" 0 "validate_battle")

    # Extract battle ID
    BATTLE_ID=$(extract_json_value "$battle_response" "id")
    if [ -z "$BATTLE_ID" ] || [ "$BATTLE_ID" = "null" ]; then
        echo -e "${YELLOW}${MSG_WARNING}: ${MSG_NO_BATTLE_ID}${NC}"
        log "${MSG_WARNING}: ${MSG_NO_BATTLE_ID}"
        BATTLE_ID="000000000000000000000002"
    else
        echo -e "${BLUE}${MSG_RETRIEVED} ${MSG_BATTLE_ID}: $BATTLE_ID${NC}"
        log "${MSG_RETRIEVED} ${MSG_BATTLE_ID}: $BATTLE_ID"
    fi
    echo ""
    log ""

    # 5.2 Get Battle Details
    run_test "Get Battle Details" "curl -s -X GET \"$API_URL/battles/$BATTLE_ID\" -H \"Authorization: Bearer $TOKEN\"" 0 "validate_battle"

    # 5.3 Get Monster's Battle History
    run_test "Get Monster's Battle History" "curl -s -X GET \"$API_URL/battles/monster/$MONSTER_ID\" -H \"Authorization: Bearer $TOKEN\"" 0

    # 5.4 Get All Battle History
    run_test "Get All Battle History" "curl -s -X GET \"$API_URL/battles/history\" -H \"Authorization: Bearer $TOKEN\"" 0
else
    skip_test "$MSG_BATTLE_TESTS" "$MSG_GROUP_NOT_SELECTED"
fi

# 6. Royal Rumble API Tests
if should_run_group "royal"; then
    print_header "$MSG_ROYAL_TESTS"

    # Get third monster ID for royal rumble testing
    if command -v jq &> /dev/null; then
        MONSTER_ID3=$(echo "$player_monsters_response" | jq -r '.[2]' 2>/dev/null)
    else
        MONSTER_ID3=$(echo "$player_monsters_response" | grep -o '"[0-9a-f]\{24\}"' | sed 's/"//g' | sed -n '3p')
    fi
    
    if [ -z "$MONSTER_ID3" ] || [ "$MONSTER_ID3" = "null" ]; then
        echo -e "${YELLOW}${MSG_WARNING}: ${MSG_NO_THIRD_MONSTER}${NC}"
        log "${MSG_WARNING}: ${MSG_NO_THIRD_MONSTER}"
        MONSTER_ID3="000000000000000000000003"
    else
        echo -e "${BLUE}${MSG_RETRIEVED} ${MSG_MONSTER_ID} 3: $MONSTER_ID3${NC}"
        log "${MSG_RETRIEVED} ${MSG_MONSTER_ID} 3: $MONSTER_ID3"
    fi
    echo ""
    log ""

    # 6.1 Start Royal Rumble
    rumble_response=$(run_test "Start Royal Rumble" "curl -s -X POST \"$API_URL/royal-rumble\" -H \"Authorization: Bearer $TOKEN\" -H \"Content-Type: application/json\" -d '{\"monsterIds\": [\"$MONSTER_ID\", \"$MONSTER_ID2\", \"$MONSTER_ID3\"]}'" 0 "validate_rumble")

    # Extract royal rumble ID
    RUMBLE_ID=$(extract_json_value "$rumble_response" "id")
    if [ -z "$RUMBLE_ID" ] || [ "$RUMBLE_ID" = "null" ]; then
        echo -e "${YELLOW}${MSG_WARNING}: ${MSG_NO_RUMBLE_ID}${NC}"
        log "${MSG_WARNING}: ${MSG_NO_RUMBLE_ID}"
        RUMBLE_ID="000000000000000000000004"
    else
        echo -e "${BLUE}${MSG_RETRIEVED} ${MSG_RUMBLE_ID}: $RUMBLE_ID${NC}"
        log "${MSG_RETRIEVED} ${MSG_RUMBLE_ID}: $RUMBLE_ID"
    fi
    echo ""
    log ""

    # 6.2 Get Experience Gained from Specific Royal Rumble
    run_test "Get Experience Gained from Specific Royal Rumble" "curl -s -X GET \"$API_URL/royal-rumble/experience/$RUMBLE_ID\" -H \"Authorization: Bearer $TOKEN\"" 0

    # 6.3 Get All Royal Rumble History
    run_test "Get All Royal Rumble History" "curl -s -X GET \"$API_URL/royal-rumble\" -H \"Authorization: Bearer $TOKEN\"" 0
else
    skip_test "$MSG_ROYAL_TESTS" "$MSG_GROUP_NOT_SELECTED"
fi

# Print Test Result Summary
print_header "$MSG_TEST_SUMMARY"
echo -e "${BLUE}${MSG_TOTAL_TESTS}: $TOTAL_TESTS${NC}"
echo -e "${GREEN}${MSG_PASSED_TESTS}: $PASSED_TESTS${NC}"
echo -e "${RED}${MSG_FAILED_TESTS}: $FAILED_TESTS${NC}"
echo -e "${YELLOW}${MSG_SKIPPED_TESTS}: $SKIPPED_TESTS${NC}"

# Calculate pass rate
if [ $TOTAL_TESTS -gt 0 ]; then
    PASS_RATE=$(echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc)
    echo -e "${YELLOW}${MSG_PASS_RATE}: $PASS_RATE%${NC}"
    log "${MSG_TOTAL_TESTS}: $TOTAL_TESTS"
    log "${MSG_PASSED_TESTS}: $PASSED_TESTS"
    log "${MSG_FAILED_TESTS}: $FAILED_TESTS"
    log "${MSG_SKIPPED_TESTS}: $SKIPPED_TESTS"
    log "${MSG_PASS_RATE}: $PASS_RATE%"
else
    echo -e "${YELLOW}${MSG_NO_TESTS}${NC}"
    log "${MSG_NO_TESTS}"
fi

# Print log file location
echo -e "${BLUE}${MSG_LOG_SAVED}: ${LOG_FILE}${NC}"

# Exit status
if [ $FAILED_TESTS -eq 0 ]; then
    if [ $TOTAL_TESTS -gt 0 ]; then
        echo -e "${GREEN}${MSG_ALL_PASSED}${NC}"
        log "${MSG_ALL_PASSED}"
    else
        echo -e "${YELLOW}${MSG_NO_TESTS}${NC}"
        log "${MSG_NO_TESTS}"
    fi
    exit 0
else
    echo -e "${RED}${MSG_SOME_FAILED}${NC}"
    log "${MSG_SOME_FAILED}"
    exit 1
fi