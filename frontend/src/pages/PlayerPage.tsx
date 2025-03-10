import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Progress, Button, Typography, Spin, message, Tabs, Empty, Modal } from 'antd';
import { UserOutlined, UpCircleOutlined, ReloadOutlined, DeleteOutlined, StarOutlined } from '@ant-design/icons';
import { playerAPI, monsterAPI } from '../services/api';
import { generateMonsterName } from '../utils/nameGenerator';

const { Title, Text } = Typography;
const { TabPane } = Tabs;

interface PlayerProfile {
  username: string;
  level: number;
  experience: number;
  maxExperience: number;
  monsters: string[];
}

interface PlayerMonster {
  id: string;
  templateId: string;
  element: string;
  level: number;
  experience: number;
  hp: number;
  atk: number;
  def: number;
  vit: number;
  skills: Skill[];
  skillPoints: number;
  name?: string;
}

interface Skill {
  num: number;
  dmg: number;
  ratio: {
    stat: string;
    percent: number;
  };
  cooldown: number;
  level: number;
  lvlMax: number;
}

const PlayerPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState<PlayerProfile>({
    username: '',
    level: 0,
    experience: 0,
    maxExperience: 100,
    monsters: []
  });
  const [monsters, setMonsters] = useState<PlayerMonster[]>([]);
  const [selectedMonster, setSelectedMonster] = useState<PlayerMonster | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchPlayerData();
  }, []);

  const fetchPlayerData = async () => {
    setLoading(true);
    setError(null);
    try {
      // Get player info
      const profileResponse = await playerAPI.getProfile();
      console.log('Profile Response:', profileResponse.data);
      
      if (profileResponse.data) {
        setProfile(profileResponse.data);
        
        // Get player's monster ID list
        try {
          const monstersResponse = await playerAPI.getMonsters();
          console.log('Player Monsters IDs Response:', monstersResponse.data);
          
          // Get all monster details
          const allMonstersResponse = await monsterAPI.getAllMonsters();
          console.log('All Monsters Response:', allMonstersResponse.data);
          
          if (Array.isArray(monstersResponse.data) && Array.isArray(allMonstersResponse.data)) {
            // Create mapping from monster ID to monster details
            const monstersMap: Record<string, any> = {};
            allMonstersResponse.data.forEach((monster: any) => {
              if (monster && monster.id) {
                monstersMap[monster.id] = monster;
              }
            });
            
            console.log('Monsters Map:', monstersMap);
            console.log('Player Monster IDs:', monstersResponse.data);
            
            // Filter out monsters owned by player
            const playerMonsters = monstersResponse.data
              .map(monsterId => {
                console.log('Processing monster ID:', monsterId, 'Found in map:', !!monstersMap[monsterId]);
                return monstersMap[monsterId];
              })
              .filter(monster => monster !== undefined);
            
            console.log('Filtered Player Monsters:', playerMonsters);
            
            setMonsters(playerMonsters);
            if (playerMonsters.length > 0) {
              // If currently selected monster is not in player's monster list, select first monster
              if (!selectedMonster || !playerMonsters.some(m => m.id === selectedMonster.id)) {
                console.log('Setting selected monster to:', playerMonsters[0]);
                setSelectedMonster(playerMonsters[0]);
              } else {
                console.log('Keeping current selected monster:', selectedMonster);
              }
            } else {
              console.log('No monsters found for player');
              setSelectedMonster(null);
            }
          } else {
            console.error('Invalid response format for monsters:', monstersResponse.data, allMonstersResponse.data);
            setError('Failed to get monster data, invalid format');
            setMonsters([]);
            setSelectedMonster(null);
          }
        } catch (error) {
          console.error('Failed to get monster data:', error);
          setError('Failed to get monster data');
          setMonsters([]);
          setSelectedMonster(null);
        }
      } else {
        console.error('Invalid profile response:', profileResponse);
        setError('Failed to get player info, invalid format');
      }
    } catch (error) {
      console.error('Failed to get player info:', error);
      setError('Failed to get player info');
    } finally {
      setLoading(false);
    }
  };

  const handleLevelUp = async () => {
    try {
      await playerAPI.levelUp();
      message.success('Level up successful!');
      fetchPlayerData();
    } catch (error) {
      console.error('Level up failed:', error);
      message.error('Level up failed');
    }
  };

  const handleAddExperience = async () => {
    try {
      await playerAPI.addExperience(10);
      message.success('Experience added successfully!');
      fetchPlayerData();
    } catch (error) {
      console.error('Failed to add experience:', error);
      message.error('Failed to add experience');
    }
  };

  const handleUpgradeSkill = async (monsterId: string, skillNum: number) => {
    try {
      await monsterAPI.upgradeSkill(monsterId, skillNum);
      message.success('Skill upgraded successfully!');
      fetchPlayerData();
    } catch (error) {
      console.error('Failed to upgrade skill:', error);
      message.error('Failed to upgrade skill');
    }
  };

  const handleDeleteMonster = async (monsterId: string) => {
    Modal.confirm({
      title: 'Confirm Delete',
      content: 'Are you sure you want to delete this monster? This action cannot be undone!',
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await playerAPI.removeMonster(monsterId);
          message.success('Monster deleted successfully!');
          fetchPlayerData();
          if (selectedMonster?.id === monsterId) {
            setSelectedMonster(null);
          }
        } catch (error) {
          console.error('Failed to delete monster:', error);
          message.error('Failed to delete monster');
        }
      }
    });
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
    return elementColors[element.toLowerCase()] || '#000000';
  };

  const renderMonsterCard = (monster: PlayerMonster) => {
    // Ensure monster has a name
    if (!monster.name) {
      monster.name = generateMonsterName(monster.id, monster.element);
    }
    
    return (
      <Card
        hoverable
        style={{ 
          marginBottom: 8, 
          borderColor: selectedMonster && selectedMonster.id === monster.id ? '#1890ff' : undefined,
          backgroundColor: selectedMonster && selectedMonster.id === monster.id ? '#e6f7ff' : undefined
        }}
        onClick={() => setSelectedMonster(monster)}
      >
        <div className="monster-card-title">
          <Text strong style={{ color: getElementColor(monster.element) }}>
            {monster.name}
          </Text>
          <Text>Lv.{monster.level}</Text>
        </div>
        <div>
          <div className="stat-item">
            <span>HP:</span>
            <span>{monster.hp}</span>
          </div>
          <div className="stat-item">
            <span>Attack:</span>
            <span>{monster.atk}</span>
          </div>
          <div className="stat-item">
            <span>Defense:</span>
            <span>{monster.def}</span>
          </div>
          <div className="stat-item">
            <span>Speed:</span>
            <span>{monster.vit}</span>
          </div>
        </div>
      </Card>
    );
  };

  const renderMonsterDetail = () => {
    if (!selectedMonster) return null;
    
    // Ensure monster has a name
    if (!selectedMonster.name) {
      selectedMonster.name = generateMonsterName(selectedMonster.id, selectedMonster.element);
    }
    
    return (
      <Card 
        title={`${selectedMonster.name} Details`}
        extra={
          <Button 
            type="primary" 
            danger 
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteMonster(selectedMonster.id)}
          >
            Delete Monster
          </Button>
        }
      >
        <Row gutter={[16, 16]}>
          <Col span={12}>
            <div className="stat-item">
              <span>Level:</span>
              <span>{selectedMonster.level}</span>
            </div>
            <div className="stat-item">
              <span>Experience:</span>
              <span>{selectedMonster.experience}</span>
            </div>
            <div className="stat-item">
              <span>HP:</span>
              <span>{selectedMonster.hp}</span>
            </div>
            <div className="stat-item">
              <span>Attack:</span>
              <span>{selectedMonster.atk}</span>
            </div>
          </Col>
          <Col span={12}>
            <div className="stat-item">
              <span>Defense:</span>
              <span>{selectedMonster.def}</span>
            </div>
            <div className="stat-item">
              <span>Speed:</span>
              <span>{selectedMonster.vit}</span>
            </div>
            <div className="stat-item">
              <span>Skill Points:</span>
              <span>{selectedMonster.skillPoints}</span>
            </div>
          </Col>
        </Row>
        
        <Title level={5} style={{ marginTop: 16 }}>Skills</Title>
        {selectedMonster.skills && selectedMonster.skills.length > 0 ? (
          selectedMonster.skills.map((skill) => (
            <Card 
              key={skill.num} 
              size="small" 
              style={{ marginBottom: 8 }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div>Skill {skill.num} (Lv.{skill.level}/{skill.lvlMax})</div>
                  <div>Damage: {skill.dmg}</div>
                  <div>Stat Bonus: {skill.ratio.stat} {skill.ratio.percent}%</div>
                  <div>Cooldown: {skill.cooldown}</div>
                </div>
                <Button 
                  type="primary" 
                  icon={<UpCircleOutlined />}
                  disabled={selectedMonster.skillPoints <= 0 || skill.level >= skill.lvlMax}
                  onClick={() => handleUpgradeSkill(selectedMonster.id, skill.num)}
                >
                  Upgrade
                </Button>
              </div>
            </Card>
          ))
        ) : (
          <Empty description="This monster has no skills" />
        )}
      </Card>
    );
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Title level={2}>Player Info</Title>
        <Button 
          icon={<ReloadOutlined />} 
          onClick={fetchPlayerData}
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
          <Card title="Player Profile">
            <Row gutter={16}>
              <Col span={24}>
                <Card>
                  <div style={{ textAlign: 'center' }}>
                    <Title level={3}>{profile.username}</Title>
                    <div style={{ marginBottom: 16 }}>
                      <Text>Level: {profile.level}</Text>
                      <Progress
                        percent={Math.round((profile.experience / profile.maxExperience) * 100)}
                        format={() => `${profile.experience} / ${profile.maxExperience}`}
                      />
                    </div>
                    <div>
                      <Button
                        type="default"
                        icon={<StarOutlined />}
                        onClick={handleAddExperience}
                      >
                        Add Experience
                      </Button>
                    </div>
                  </div>
                </Card>
              </Col>
            </Row>
          </Card>
          
          <Tabs defaultActiveKey="monsters" style={{ marginTop: 16 }}>
            <TabPane tab="Monster List" key="monsters">
              <div style={{ marginTop: 16 }}>
                {monsters.length > 0 ? (
                  <Row gutter={[16, 16]}>
                    <Col span={8}>
                      <div style={{ 
                        display: 'flex', 
                        flexDirection: 'column',
                        height: '500px',
                        overflowY: 'auto'
                      }}>
                        {monsters.map(monster => renderMonsterCard(monster))}
                      </div>
                    </Col>
                    <Col span={16}>
                      {renderMonsterDetail()}
                    </Col>
                  </Row>
                ) : (
                  <Empty description="You don't have any monsters yet. Go to the Summon page to get new monsters!" />
                )}
              </div>
            </TabPane>
          </Tabs>
        </>
      )}
    </div>
  );
};

export default PlayerPage;