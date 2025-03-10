import React, { useState, useEffect } from 'react';
import { Card, Button, Typography, Spin, message, List, Tag, Modal, Result, Empty, Alert, Progress, Row, Col } from 'antd';
import { GiftOutlined, HistoryOutlined, ReloadOutlined } from '@ant-design/icons';
import { summonAPI, playerAPI, monsterAPI } from '../services/api';
import { generateMonsterName } from '../utils/nameGenerator';
import moment from 'moment';

const { Title, Text } = Typography;

interface SummonLog {
  id: string;
  username: string;
  monsterId: string;
  element: string;
  level: number;
  date: string;
}

interface SummonResult {
  id: string;
  element: string;
  level: number;
  hp: number;
  atk: number;
  def: number;
  vit: number;
  skills: any[];
}

interface PlayerProfile {
  username: string;
  level: number;
  experience: number;
  maxExperience: number;
  monsters: string[];
  maxMonsters: number;
}

const SummoningPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [summoning, setSummoning] = useState(false);
  const [summonHistory, setSummonHistory] = useState<SummonLog[]>([]);
  const [summonResult, setSummonResult] = useState<SummonResult | null>(null);
  const [multiSummonResults, setMultiSummonResults] = useState<SummonResult[]>([]);
  const [showResultModal, setShowResultModal] = useState(false);
  const [showMultiResultModal, setShowMultiResultModal] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [playerProfile, setPlayerProfile] = useState<PlayerProfile | null>(null);
  const [summoningProgress, setSummoningProgress] = useState<number>(0);
  const [summoningTotal, setSummoningTotal] = useState<number>(0);
  const [showProgressModal, setShowProgressModal] = useState<boolean>(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Get player info
      const profileResponse = await playerAPI.getProfile();
      console.log('Player Profile Response:', profileResponse.data);
      
      if (profileResponse.data) {
        setPlayerProfile(profileResponse.data);
      }
      
      // Get summon history
      await fetchSummonHistory();
    } catch (error: any) {
      console.error('Failed to fetch data:', error);
      if (error.response) {
        setError(`Failed to fetch data: ${error.response.status} ${error.response.statusText}`);
      } else if (error.request) {
        setError('Unable to connect to server, please check your network connection');
      } else {
        setError(`Request error: ${error.message}`);
      }
      message.error('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  const fetchSummonHistory = async () => {
    try {
      const response = await summonAPI.getSummonHistory();
      console.log('Summon History Response:', response.data);
      
      if (response.data && Array.isArray(response.data)) {
        // Get summon history records
        const summonLogs = response.data;
        
        // Create an enhanced summon history array
        const enhancedLogs = await Promise.all(
          summonLogs.map(async (log) => {
            // Try to get element info from monster API (if needed)
            let element = log.element;
            
            if (!element && log.monsterId) {
              try {
                const monsterResponse = await monsterAPI.getMonsterById(log.monsterId);
                if (monsterResponse.data) {
                  element = monsterResponse.data.element;
                }
              } catch (error) {
                console.error(`Failed to get monster info (ID: ${log.monsterId}):`, error);
              }
            }
            
            // Handle date field (backend may use summonDate instead of date)
            const dateValue = log.date || log.summonDate;
            
            // Return enhanced log, always set level to 1 (initial level at summon)
            return {
              ...log,
              element: element || 'unknown',
              level: 1, // Summoned monsters always start at level 1
              date: dateValue // Ensure date field exists
            };
          })
        );
        
        // Sort by date from newest to oldest
        const sortedLogs = enhancedLogs.sort((a, b) => {
          // If no date, put at end
          if (!a.date) return 1;
          if (!b.date) return -1;
          
          // Convert dates to timestamps for comparison
          const dateA = new Date(a.date).getTime();
          const dateB = new Date(b.date).getTime();
          
          // Sort descending (newest to oldest)
          return dateB - dateA;
        });
        
        setSummonHistory(sortedLogs);
      } else {
        console.warn('Summon history data is not in array format:', response.data);
        setSummonHistory([]);
      }
    } catch (error: any) {
      console.error('Failed to get summon history:', error);
      setSummonHistory([]);
      throw error; // Pass error up for fetchData to handle
    }
  };

  const summonMonster = async () => {
    // Check if player monster count is at limit
    if (playerProfile && playerProfile.monsters.length >= playerProfile.maxMonsters) {
      message.error(`You have reached your monster limit (${playerProfile.maxMonsters}), please level up or remove some monsters first`);
      return;
    }
    
    setSummoning(true);
    setError(null);
    try {
      const response = await summonAPI.summonMonster();
      console.log('Summon Response:', response.data);
      
      if (response.data) {
        setSummonResult(response.data);
        setShowResultModal(true);
        message.success('Summon successful!');
        // Refresh data
        fetchData();
      } else {
        setError('Summon failed, server returned empty data');
        message.error('Summon failed');
      }
    } catch (error: any) {
      console.error('Summon failed:', error);
      if (error.response) {
        if (error.response.status === 500 && error.response.data && error.response.data.message && error.response.data.message.includes('Failed to add monster to player')) {
          setError('You have reached your monster limit, please level up or remove some monsters first');
        } else {
          setError(`Summon failed: ${error.response.status} ${error.response.statusText}`);
        }
      } else if (error.request) {
        setError('Unable to connect to server, please check your network connection');
      } else {
        setError(`Request error: ${error.message}`);
      }
      message.error('Summon failed');
    } finally {
      setSummoning(false);
    }
  };

  const summonMultiple = async () => {
    // Check if player monster count is near limit
    if (playerProfile) {
      const availableSlots = playerProfile.maxMonsters - playerProfile.monsters.length;
      if (availableSlots <= 0) {
        message.error(`You have reached your monster limit (${playerProfile.maxMonsters}), please level up or remove some monsters first`);
        return;
      }
      
      if (availableSlots < 10) {
        message.warning(`You only have ${availableSlots} slots available, will only summon ${availableSlots} monsters`);
      }
    }
    
    setSummoning(true);
    setError(null);
    
    // Determine number of summons
    const availableSlots = playerProfile ? playerProfile.maxMonsters - playerProfile.monsters.length : 10;
    const summonCount = Math.min(10, availableSlots);
    
    // Set initial progress indicator values
    setSummoningProgress(0);
    setSummoningTotal(summonCount);
    setShowProgressModal(true);
    
    try {
      // Try to use batch summon API
      try {
        // Update progress to start state
        setSummoningProgress(0);
        
        // Add delay to make animation more visible
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // Call batch summon API
        const response = await summonAPI.summonMultiple(summonCount);
        console.log(`Batch summon results:`, response.data);
        
        if (response.data && Array.isArray(response.data) && response.data.length > 0) {
          // Complete all summons, set progress to 100%
          setSummoningProgress(summonCount);
          
          // Brief delay before closing progress bar and showing results
          await new Promise(resolve => setTimeout(resolve, 500));
          setShowProgressModal(false);
          
          setMultiSummonResults(response.data);
          setShowMultiResultModal(true);
          message.success(`Successfully summoned ${response.data.length} monsters!`);
          // Refresh data
          fetchData();
          return;
        }
      } catch (error) {
        console.error('Batch summon failed, will try individual summons:', error);
        // If batch summon fails, fall back to individual summons
      }
      
      // If batch summon fails, fall back to individual summons
      // Create array to store summon results
      const results: SummonResult[] = [];
      
      // Loop through individual summons
      for (let i = 0; i < summonCount; i++) {
        try {
          // Update progress
          setSummoningProgress(i);
          
          // Add delay to make animation more visible
          await new Promise(resolve => setTimeout(resolve, 500));
          
          const response = await summonAPI.summonMonster();
          console.log(`Summon #${i+1} result:`, response.data);
          
          if (response.data) {
            results.push(response.data);
          }
        } catch (error) {
          console.error(`Summon #${i+1} failed:`, error);
          // If individual summon fails, continue to next one
        }
      }
      
      // Complete all summons, set progress to 100%
      setSummoningProgress(summonCount);
      
      // Brief delay before closing progress bar and showing results
      await new Promise(resolve => setTimeout(resolve, 500));
      setShowProgressModal(false);
      
      // Check if any summons were successful
      if (results.length > 0) {
        setMultiSummonResults(results);
        setShowMultiResultModal(true);
        message.success(`Successfully summoned ${results.length} monsters!`);
        // Refresh data
        fetchData();
      } else {
        setError('All summons failed, please try again later');
        message.error('Summon failed');
      }
    } catch (error: any) {
      setShowProgressModal(false);
      console.error('Summon failed:', error);
      if (error.response) {
        if (error.response.status === 500 && error.response.data && error.response.data.message && error.response.data.message.includes('You have reached your monster limit')) {
          setError('You have reached your monster limit, please level up or remove some monsters first');
        } else {
          setError(`Summon failed: ${error.response.status} ${error.response.statusText}`);
        }
      } else if (error.request) {
        setError('Unable to connect to server, please check your network connection');
      } else {
        setError(`Request error: ${error.message}`);
      }
      message.error('Summon failed');
    } finally {
      setSummoning(false);
    }
  };

  const getElementColor = (element: string) => {
    const elementColors: Record<string, string> = {
      fire: '#f5222d',
      water: '#1890ff',
      earth: '#52c41a',
      wind: '#722ed1',
      light: '#faad14',
      dark: '#722ed1'
    };
    return elementColors[element?.toLowerCase()] || '#000000';
  };

  const renderSummonResult = () => {
    if (!summonResult) return null;

    // Generate monster name
    const monsterName = generateMonsterName(summonResult.id, summonResult.element);

    return (
      <Modal
        title="Summon Result"
        open={showResultModal}
        onCancel={() => setShowResultModal(false)}
        footer={[
          <Button key="close" onClick={() => setShowResultModal(false)}>
            Close
          </Button>
        ]}
        width={600}
      >
        <Result
          status="success"
          title="Summon Successful!"
          subTitle="You got a new monster!"
        />
        
        <Card>
          <div style={{ textAlign: 'center', marginBottom: 16 }}>
            <Tag color={getElementColor(summonResult.element)} style={{ fontSize: 16, padding: '4px 8px' }}>
              {monsterName} (Lv.{summonResult.level})
            </Tag>
          </div>
          
          <div style={{ display: 'flex', justifyContent: 'space-around' }}>
            <div>
              <div className="stat-item">
                <span>HP:</span>
                <span>{summonResult.hp}</span>
              </div>
              <div className="stat-item">
                <span>Attack:</span>
                <span>{summonResult.atk}</span>
              </div>
            </div>
            <div>
              <div className="stat-item">
                <span>Defense:</span>
                <span>{summonResult.def}</span>
              </div>
              <div className="stat-item">
                <span>Speed:</span>
                <span>{summonResult.vit}</span>
              </div>
            </div>
          </div>
          
          <div style={{ marginTop: 16 }}>
            <Title level={5}>Skills</Title>
            {summonResult.skills && summonResult.skills.length > 0 ? (
              summonResult.skills.map((skill: any, index: number) => (
                <div key={index} className="stat-item">
                  <span>Skill {skill.num}:</span>
                  <span>Damage {skill.dmg} | Cooldown {skill.cooldown}</span>
                </div>
              ))
            ) : (
              <Empty description="This monster has no skills" />
            )}
          </div>
        </Card>
      </Modal>
    );
  };

  const renderMultiSummonResults = () => {
    if (!multiSummonResults || multiSummonResults.length === 0) return null;
    
    return (
      <Modal
        title="Summon Results"
        open={showMultiResultModal}
        onCancel={() => setShowMultiResultModal(false)}
        footer={[
          <Button key="close" onClick={() => setShowMultiResultModal(false)}>
            Close
          </Button>
        ]}
        width={800}
      >
        <Result
          status="success"
          title="Congratulations! You summoned multiple monsters"
          subTitle={`Successfully summoned ${multiSummonResults.length} monsters`}
        />
        
        <Row gutter={[16, 16]}>
          {multiSummonResults.map((monster, index) => (
            <Col key={monster.id} xs={24} sm={12} md={8}>
              <Card 
                title={`Monster #${index + 1}`}
                bordered
                style={{ marginBottom: 16 }}
              >
                <div>
                  <Tag color={getElementColor(monster.element)}>
                    {generateMonsterName(monster.id, monster.element)} (Lv.{monster.level})
                  </Tag>
                </div>
                <div style={{ marginTop: 8 }}>
                  <div>Level: {monster.level}</div>
                  <div>HP: {monster.hp}</div>
                  <div>Attack: {monster.atk}</div>
                  <div>Defense: {monster.def}</div>
                  <div>Speed: {monster.vit}</div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </Modal>
    );
  };

  // Render summoning progress modal
  const renderSummoningProgress = () => {
    return (
      <Modal
        title="Summoning Monster"
        open={showProgressModal}
        footer={null}
        closable={false}
        centered
      >
        <div style={{ textAlign: 'center', padding: '20px 0' }}>
          <div style={{ marginBottom: 20 }}>
            <img 
              src="/summon-animation.gif" 
              alt="Summoning" 
              style={{ width: 150, height: 150, objectFit: 'cover' }}
              onError={(e) => {
                // If image fails to load, show fallback content
                e.currentTarget.style.display = 'none';
              }}
            />
          </div>
          <Typography.Title level={4}>
            Summoning Monster {summoningProgress + 1} of {summoningTotal}
          </Typography.Title>
          <Progress 
            percent={Math.round((summoningProgress / summoningTotal) * 100)} 
            status="active" 
            strokeColor={{
              '0%': '#108ee9',
              '100%': '#87d068',
            }}
          />
          <Typography.Text type="secondary" style={{ display: 'block', marginTop: 10 }}>
            Please wait while summoning powerful monsters from another world...
          </Typography.Text>
        </div>
      </Modal>
    );
  };

  // Calculate monster count and limit
  const monsterCount = playerProfile ? playerProfile.monsters.length : 0;
  const monsterLimit = playerProfile ? playerProfile.maxMonsters : 0;
  const isMonsterLimitReached = monsterCount >= monsterLimit;

  // Add date formatting function
  const formatDate = (dateString: string | Date | undefined) => {
    if (!dateString) return 'Unknown';
    try {
      return moment(dateString).format('YYYY-MM-DD HH:mm:ss');
    } catch (error) {
      console.error('Date formatting error:', error);
      return 'Unknown';
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={2}>Monster Summoning</Title>
        <Button 
          icon={<ReloadOutlined />} 
          onClick={fetchData}
          loading={loading}
        >
          Refresh Data
        </Button>
      </div>
      
      {error && (
        <Card style={{ marginBottom: 16, backgroundColor: '#fff2f0', borderColor: '#ffccc7' }}>
          <div style={{ color: '#cf1322' }}>{error}</div>
        </Card>
      )}
      
      {loading ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          <Card title="Summon New Monster">
            <div style={{ textAlign: 'center' }}>
              <div style={{ marginBottom: 16 }}>
                {playerProfile && (
                  <div style={{ marginBottom: 8 }}>
                    Current Monsters: {playerProfile.monsters.length} / {playerProfile.maxMonsters}
                  </div>
                )}
                <Button
                  type="primary"
                  icon={<GiftOutlined />}
                  size="large"
                  onClick={summonMonster}
                  loading={summoning}
                  style={{ marginRight: 16 }}
                >
                  Summon Monster
                </Button>
                <Button
                  type="primary"
                  icon={<GiftOutlined />}
                  size="large"
                  onClick={summonMultiple}
                  loading={summoning}
                >
                  Summon 10 Times
                </Button>
              </div>
              <div>
                <Text type="secondary">
                  Summon a random monster to join your team. Each monster has different elements and attributes.
                </Text>
              </div>
            </div>
          </Card>
          
          <Card title={<><HistoryOutlined /> Summon History</>} style={{ marginTop: 16 }}>
            {summonHistory.length > 0 ? (
              <List
                dataSource={summonHistory}
                renderItem={(log) => (
                  <List.Item>
                    <List.Item.Meta
                      title={
                        <Tag color={getElementColor(log.element)}>
                          {generateMonsterName(log.monsterId, log.element)} (Lv.{log.level || '?'})
                        </Tag>
                      }
                      description={`Summon Time: ${formatDate(log.date)}`}
                    />
                    <div>
                      ID: {log.monsterId}
                    </div>
                  </List.Item>
                )}
                pagination={{
                  pageSize: 5,
                  hideOnSinglePage: true
                }}
              />
            ) : (
              <Empty description="No summon records" />
            )}
          </Card>
          
          {renderSummonResult()}
          {renderMultiSummonResults()}
          {renderSummoningProgress()}
        </>
      )}
    </div>
  );
};

export default SummoningPage;