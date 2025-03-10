import React, { useState } from 'react';
import { Form, Input, Button, message, Alert } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { authAPI } from '../services/api';

interface LoginProps {
  onLoginSuccess: (token: string) => void;
}

interface LoginFormValues {
  username: string;
  password: string;
}

const Login: React.FC<LoginProps> = ({ onLoginSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onFinish = async (values: LoginFormValues) => {
    setLoading(true);
    setError(null);
    try {
      const response = await authAPI.login(values.username, values.password);
      console.log('Login Response:', response.data);
      
      if (response.data && response.data.token) {
        message.success('Login successful!');
        // Save username to local storage
        localStorage.setItem('username', values.username);
        // Call login success callback
        onLoginSuccess(response.data.token);
      } else {
        setError('Login failed, invalid server response format');
        message.error('Login failed, please check username and password!');
      }
    } catch (error: any) {
      console.error('Login error:', error);
      
      if (error.response) {
        // Server returned error status code
        if (error.response.status === 401) {
          setError('Invalid username or password');
        } else if (error.response.status === 500) {
          setError('Internal server error, please try again later');
        } else {
          setError(`Server error: ${error.response.status}`);
        }
      } else if (error.request) {
        // Request sent but no response received
        setError('Unable to connect to server, please check network connection');
      } else {
        // Error setting up request
        setError(`Request error: ${error.message}`);
      }
      
      message.error('Login failed, please check username and password!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-form">
        <h1 className="login-title">Gatcha Game System</h1>
        
        {error && (
          <Alert
            message="Login Failed"
            description={error}
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
            closable
            onClose={() => setError(null)}
          />
        )}
        
        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Please input your username!' }]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="Username" 
              size="large"
            />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please input your password!' }]}
          >
            <Input
              prefix={<LockOutlined />}
              type="password"
              placeholder="Password"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              className="login-form-button"
              loading={loading}
              size="large"
            >
              Login
            </Button>
          </Form.Item>
          
          <div style={{ textAlign: 'center' }}>
            <p>Test Accounts:</p>
            <p>Username: user1, Password: password1</p>
            <p>Username: user2, Password: password2</p>
          </div>
        </Form>
      </div>
    </div>
  );
};

export default Login;