 "use client";
 import { createContext, useState, useContext, useEffect } from 'react';

 // Create context
 const AuthContext = createContext();

 // Auth Provider Component
 export function AuthProvider({ children }) {
   const [user, setUser] = useState(null);
   const [loading, setLoading] = useState(true);

   useEffect(() => {
     // Check if user is logged in
     const savedUser = localStorage.getItem('user');
     if (savedUser) {
       setUser(JSON.parse(savedUser));
     }
     setLoading(false);
   }, []);

   const login = (userData, token) => {
     // ✅ Proper token validation
     if (token && typeof token === 'string' && token.length > 20 &&
         !token.includes('fake') && !token.includes('demo')) {

       setUser(userData);
       localStorage.setItem('user', JSON.stringify(userData));
       localStorage.setItem('token', token);

     } else {
       // Demo mode - only save user, no token
       setUser(userData);
       localStorage.setItem('user', JSON.stringify(userData));
       localStorage.setItem('token', 'demo-mode-no-real-token');
     }
   };

   const logout = () => {
     setUser(null);
     localStorage.removeItem('user');
     localStorage.removeItem('token');
   };

   const apiCall = async (url, options = {}) => {
     try {
       const token = localStorage.getItem('token');

       const config = {
         method: options.method || 'GET',
         headers: {
           'Content-Type': 'application/json',
           ...options.headers,
         },
         ...options,
       };

       // ✅ Proper token handling
       if (token && token !== 'demo-mode-no-real-token' &&
           !token.includes('fake') && !token.includes('demo')) {
         config.headers['Authorization'] = `Bearer ${token}`;
       }

       // ✅ Remove duplicate body assignment
       if (options.body && typeof options.body === 'object') {
         config.body = JSON.stringify(options.body);
       }

       const response = await fetch(`https://budget-based-restaurant-menu-system.onrender.com/api${url}`, config);

       if (!response.ok) {
         const errorText = await response.text();
         throw new Error(`API error: ${response.status} - ${errorText}`);
       }

       const data = await response.json();
       return data;

     } catch (error) {
       console.error('API Call Failed:', error);
       throw error;
     }
   };

   const value = {
     user,
     login,
     logout,
     loading,
     apiCall
   };

   return (
     <AuthContext.Provider value={value}>
       {children}
     </AuthContext.Provider>
   );
 }

 // Use Auth Hook
 export function useAuth() {
   const context = useContext(AuthContext);
   if (!context) {
     throw new Error('useAuth must be used within AuthProvider');
   }
   return context;
 }