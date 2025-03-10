import React, { useState } from 'react';
import { Layout, Menu, Button, theme } from 'antd';
import { 
  DashboardOutlined, 
  UserOutlined, 
  ThunderboltOutlined, 
  GiftOutlined, 
  TeamOutlined,
  LogoutOutlined
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Header, Content, Sider } = Layout;

interface MainLayoutProps {
  onLogout: () => void;
}

const MainLayout: React.FC<MainLayoutProps> = ({ onLogout }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { token } = theme.useToken();

  // Get current username
  const username = localStorage.getItem('username') || 'Player';

  // Menu items configuration
  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/player',
      icon: <UserOutlined />,
      label: 'Player Info',
    },
    {
      key: '/battle',
      icon: <ThunderboltOutlined />,
      label: 'Battle',
    },
    {
      key: '/summoning',
      icon: <GiftOutlined />,
      label: 'Summon',
    },
    {
      key: '/royal-rumble',
      icon: <TeamOutlined />,
      label: 'Royal Rumble',
    },
  ];

  // Handle menu click
  const handleMenuClick = (key: string) => {
    navigate(key);
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider 
        collapsible 
        collapsed={collapsed} 
        onCollapse={(value) => setCollapsed(value)}
      >
        <div className="logo">
          {collapsed ? 'G' : 'Gatcha'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => handleMenuClick(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ padding: 0, background: token.colorBgContainer }}>
          <div style={{ display: 'flex', justifyContent: 'flex-end', paddingRight: 24 }}>
            <span style={{ marginRight: 16 }}>Welcome, {username}</span>
            <Button 
              icon={<LogoutOutlined />} 
              onClick={onLogout}
              type="link"
            >
              Logout
            </Button>
          </div>
        </Header>
        <Content style={{ margin: '16px' }}>
          <div className="site-layout-content">
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout; 