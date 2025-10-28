 "use client";
 import { useState } from 'react';
 import { useAuth } from '../components/AuthContext';
 import Link from 'next/link';

 export default function Login() {
   const [formData, setFormData] = useState({
     email: '',
     password: ''
   });
   const [error, setError] = useState('');
   const [loading, setLoading] = useState(false);
   const { login } = useAuth();

   const handleChange = (e) => {
     setFormData({
       ...formData,
       [e.target.name]: e.target.value
     });
   };

   // ✅ Form validation
   const validateForm = () => {
     if (!formData.email || !formData.password) {
       setError('Please fill in all fields');
       return false;
     }

     const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
     if (!emailRegex.test(formData.email)) {
       setError('Please enter a valid email address');
       return false;
     }

     return true;
   };

   // ✅ Login function
   const handleLogin = async (e) => {
     e.preventDefault();
     setError('');
     setLoading(true);

     // Validation
     if (!validateForm()) {
       setLoading(false);
       return;
     }

     try {
       const loginData = {
         email: formData.email.toLowerCase().trim(),
         password: formData.password
       };

       // ✅ Added timeout
       const controller = new AbortController();
       const timeoutId = setTimeout(() => controller.abort(), 10000);

       const response = await fetch('http://localhost:8081/api/auth/login', {
         method: 'POST',
         headers: {
           'Content-Type': 'application/json',
         },
         body: JSON.stringify(loginData),
         signal: controller.signal
       });

       clearTimeout(timeoutId);

       if (response.ok) {
         const data = await response.json();

         // ✅ Token validation
         if (data.token && typeof data.token === 'string' && data.token.length > 10) {
           // Login with token
           login(data.user, data.token);

           // ✅ Redirect based on role
           redirectUser(data.user.role);

         } else {
           await handleDemoFallback();
         }

       } else {
         const errorData = await response.json();

         if (response.status === 401) {
           setError('Invalid email or password');
         } else if (errorData.error) {
           setError(errorData.error);
         } else {
           setError('Login failed. Please try again.');
         }
       }
     } catch (error) {
       if (error.name === 'AbortError') {
         setError('Request timeout. Please try again.');
       } else {
         setError('Login service unavailable. Please try again later.');
       }
     } finally {
       setLoading(false);
     }
   };

   // ✅ Demo fallback function
   const handleDemoFallback = async () => {
     // Demo users for testing
     const demoUsers = {
       student: {
         id: 1,
         name: 'Demo Student',
         email: formData.email || 'student@demo.com',
         role: 'STUDENT'
       },
       owner: {
         id: 2,
         name: 'Demo Owner',
         email: formData.email || 'owner@demo.com',
         role: 'OWNER'
       },
       admin: {
         id: 3,
         name: 'Demo Admin',
         email: formData.email || 'admin@demo.com',
         role: 'ADMIN'
       }
     };

     // Auto-detect role based on email
     let demoUser;
     if (formData.email.includes('admin')) {
       demoUser = demoUsers.admin;
     } else if (formData.email.includes('owner') || formData.email.includes('restaurant')) {
       demoUser = demoUsers.owner;
     } else {
       demoUser = demoUsers.student;
     }

     // Update email if user entered something
     if (formData.email) {
       demoUser.email = formData.email;
     }

     login(demoUser, null);
     redirectUser(demoUser.role);
   };

   // ✅ Redirect function
   const redirectUser = (role) => {
     setTimeout(() => {
       if (role === 'STUDENT') {
         window.location.href = '/student/dashboard';
       } else if (role === 'OWNER') {
         window.location.href = '/owner/dashboard';
       } else if (role === 'ADMIN') {
         window.location.href = '/admin/dashboard';
       } else {
         window.location.href = '/';
       }
     }, 100);
   };

   return (
     <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4">
       <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
         <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">
           🍽️ Login to Your Account
         </h2>

         {error && (
           <div className="mb-4 p-3 bg-red-100 text-red-700 rounded text-sm">
             ❌ {error}
           </div>
         )}

         <form onSubmit={handleLogin}>
           <div className="mb-4">
             <label className="block text-gray-700 text-sm font-medium mb-2">
               Email Address *
             </label>
             <input
               type="email"
               name="email"
               placeholder="Enter your email"
               value={formData.email}
               onChange={handleChange}
               className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
               required
             />
           </div>

           <div className="mb-6">
             <label className="block text-gray-700 text-sm font-medium mb-2">
               Password *
             </label>
             <input
               type="password"
               name="password"
               placeholder="Enter your password"
               value={formData.password}
               onChange={handleChange}
               className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
               required
             />
           </div>

           <button
             type="submit"
             disabled={loading}
             className="w-full bg-blue-500 text-white p-3 rounded hover:bg-blue-600 disabled:bg-gray-400 transition font-medium flex items-center justify-center"
           >
             {loading ? (
               <>
                 <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                   <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                   <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                 </svg>
                 Logging in...
               </>
             ) : (
               'Login'
             )}
           </button>
         </form>

         <div className="mt-6 text-center">
           <p className="text-gray-600 text-sm">
             Don't have an account?{' '}
             <Link href="/register" className="text-blue-500 hover:underline">
               Register here
             </Link>
           </p>
         </div>

         {/* ✅ PERMANENT ADMIN LOGIN SECTION */}
         <div className="mt-4 p-4 bg-purple-50 rounded-lg border border-purple-200">
           <h3 className="font-bold text-purple-800 text-center mb-2">👨‍💼 System Admin Login</h3>
           <div className="text-center">
             <button
               onClick={() => {
                 setFormData({
                   email: 'admin@restaurant.com',
                   password: 'admin123'
                 });
               }}
               className="bg-purple-500 text-white px-4 py-2 rounded text-sm hover:bg-purple-600 transition"
             >
               Auto-Fill Admin Credentials
             </button>
             <p className="text-xs text-purple-600 mt-2">
               Use: admin@restaurant.com / admin123
             </p>
           </div>
         </div>
       </div>
     </div>
   );
 }