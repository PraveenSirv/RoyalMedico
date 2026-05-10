import api from '../api/axios';

export const AuthService = {
  async getUserProfile() {
    const response = await api.get('/api/user/profile');
    return response.data;
  }
};
