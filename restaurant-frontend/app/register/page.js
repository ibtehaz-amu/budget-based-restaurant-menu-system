 "use client";
 import { useState } from 'react';
 import { useAuth } from '../components/AuthContext';
 import Link from 'next/link';

 export default function Register() {
   const [formData, setFormData] = useState({
     name: '',
     email: '',
     password: '',
     confirmPassword: '',
     role: 'STUDENT',
     phone: '',
     restaurantName: '',
     restaurantAddress: ''
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

   const handleRegister = async (e) => {
     e.preventDefault();
     setError('');
     setLoading(true);

     // Validation
     if (formData.password !== formData.confirmPassword) {
       setError('Passwords do not match');
       setLoading(false);
       return;
     }

     if (formData.password.length < 6) {
       setError('Password must be at least 6 characters');
       setLoading(false);
       return;
     }

     // ✅ Phone validation for owners
     if (formData.role === 'OWNER' && (!formData.phone || formData.phone.trim().length < 10)) {
       setError('Valid phone number (min 10 digits) is required for restaurant owners');
       setLoading(false);
       return;
     }

     if (formData.role === 'OWNER' && (!formData.restaurantName || !formData.restaurantAddress)) {
       setError('Restaurant name and address are required for owners');
       setLoading(false);
       return;
     }

     try {
       let registerData = {
         name: formData.name,
         email: formData.email,
         password: formData.password,
         role: formData.role,
         phoneNumber: formData.phone || ''
       };

       // ✅ Real registration attempt
       const response = await fetch('http://localhost:8081/api/auth/register', {
         method: 'POST',
         headers: {
           'Content-Type': 'application/json',
         },
         body: JSON.stringify(registerData)
       });

       if (response.ok) {
         const data = await response.json();

         // ✅ Real token check
         if (data.token && !data.token.includes('fake') && !data.token.includes('demo')) {
           // ✅ Real login with real token
           login(data.user, data.token);

           // ✅ If owner, register restaurant WITH REAL TOKEN
           if (formData.role === 'OWNER' && formData.restaurantName && formData.restaurantAddress) {
             try {
               // ✅ FIRST: Get current user details to set as owner
               const userResponse = await fetch('http://localhost:8081/api/auth/test-token', {
                 method: 'GET',
                 headers: {
                   'Authorization': `Bearer ${data.token}`
                 }
               });

               if (userResponse.ok) {
                 // ✅ NOW create restaurant with owner information
                 const restaurantResponse = await fetch('http://localhost:8081/api/restaurants', {
                   method: 'POST',
                   headers: {
                     'Content-Type': 'application/json',
                     'Authorization': `Bearer ${data.token}`
                   },
                   body: JSON.stringify({
                     name: formData.restaurantName,
                     address: formData.restaurantAddress,
                     openingTime: '09:00',
                     closingTime: '22:00'
                   })
                 });

                 if (!restaurantResponse.ok) {
                   console.log('User registered but restaurant creation failed');
                 }
               }
             } catch (restaurantError) {
               console.log('Restaurant registration error');
             }
           }

           // ✅ Redirect after REAL registration
           if (data.user.role === 'STUDENT') {
             window.location.href = '/student/dashboard';
           } else if (data.user.role === 'OWNER') {
             window.location.href = '/owner/dashboard';
           }

         } else {
           // Real registration but fake token
           await handleDemoFallback();
         }

       } else {
         // Real registration failed
         const errorData = await response.json();

         if (errorData.error && errorData.error.includes('already exists')) {
           setError('Email already exists. Please use a different email.');
         } else {
           setError(errorData.error || 'Registration failed. Please try again.');
         }
       }
     } catch (error) {
       setError('Registration service unavailable. Please try again later.');
     } finally {
       setLoading(false);
     }
   };

   // ✅ Demo fallback function
   const handleDemoFallback = async () => {
     const demoUser = {
       user: {
         id: Math.floor(Math.random() * 1000) + 1,
         name: formData.name,
         email: formData.email,
         role: formData.role
       },
       token: null
     };

     login(demoUser.user, demoUser.token);

     if (formData.role === 'STUDENT') {
       window.location.href = '/student/dashboard';
     } else if (formData.role === 'OWNER') {
       window.location.href = '/owner/dashboard';
     }
   };

   return (
     <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4">
       <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
         <h2 className="text-2xl font-bold mb-6 text-center text-gray-800">
           🍽️ Create Account
         </h2>

         {error && (
           <div className="mb-4 p-3 bg-red-100 text-red-700 rounded text-sm">
             ❌ {error}
           </div>
         )}

         <form onSubmit={handleRegister}>
           {/* Basic Information */}
           <div className="mb-4">
             <label className="block text-gray-700 text-sm font-medium mb-2">
               Full Name *
             </label>
             <input
               type="text"
               name="name"
               placeholder="Enter your full name"
               value={formData.name}
               onChange={handleChange}
               className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
               required
             />
           </div>

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

           <div className="mb-4">
             <label className="block text-gray-700 text-sm font-medium mb-2">
               Password *
             </label>
             <input
               type="password"
               name="password"
               placeholder="Enter password (min 6 characters)"
               value={formData.password}
               onChange={handleChange}
               className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
               required
             />
           </div>

           <div className="mb-4">
             <label className="block text-gray-700 text-sm font-medium mb-2">
               Confirm Password *
             </label>
             <input
               type="password"
               name="confirmPassword"
               placeholder="Confirm your password"
               value={formData.confirmPassword}
               onChange={handleChange}
               className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
               required
             />
           </div>

           <div className="mb-4">
             <label className="block text-gray-700 text-sm font-medium mb-2">
               Phone Number {formData.role === 'OWNER' && '*'}
             </label>
             <input
               type="tel"
               name="phone"
               placeholder="Enter phone number"
               value={formData.phone}
               onChange={handleChange}
               className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
               required={formData.role === 'OWNER'}
             />
             {formData.role === 'OWNER' && (
               <p className="text-xs text-gray-500 mt-1">Phone required for restaurant owners</p>
             )}
           </div>

           {/* Role Selection */}
           <div className="mb-4">
             <label className="block text-gray-700 text-sm font-medium mb-2">
               I am a *
             </label>
             <div className="flex gap-4">
               <label className="flex items-center">
                 <input
                   type="radio"
                   name="role"
                   value="STUDENT"
                   checked={formData.role === 'STUDENT'}
                   onChange={handleChange}
                   className="mr-2"
                 />
                 Student 👨‍🎓
               </label>
               <label className="flex items-center">
                 <input
                   type="radio"
                   name="role"
                   value="OWNER"
                   checked={formData.role === 'OWNER'}
                   onChange={handleChange}
                   className="mr-2"
                 />
                 Restaurant Owner 🏪
               </label>
             </div>
           </div>

           {/* Restaurant Details (Only for Owners) */}
           {formData.role === 'OWNER' && (
             <div className="mb-4 p-4 border border-blue-200 rounded-lg bg-blue-50">
               <h3 className="font-medium text-blue-800 mb-3">Restaurant Information</h3>

               <div className="mb-3">
                 <label className="block text-gray-700 text-sm font-medium mb-2">
                   Restaurant Name *
                 </label>
                 <input
                   type="text"
                   name="restaurantName"
                   placeholder="Enter restaurant name"
                   value={formData.restaurantName}
                   onChange={handleChange}
                   className="w-full border border-gray-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   required
                 />
               </div>

               <div className="mb-3">
                 <label className="block text-gray-700 text-sm font-medium mb-2">
                   Restaurant Address *
                 </label>
                 <textarea
                   name="restaurantAddress"
                   placeholder="Enter full restaurant address"
                   value={formData.restaurantAddress}
                   onChange={handleChange}
                   rows="3"
                   className="w-full border border-gray-300 p-2 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   required
                 />
               </div>
             </div>
           )}

           <button
             type="submit"
             disabled={loading}
             className="w-full bg-blue-500 text-white p-3 rounded hover:bg-blue-600 disabled:bg-gray-400 transition font-medium"
           >
             {loading ? 'Creating Account...' : 'Create Account'}
           </button>
         </form>

         <div className="mt-6 text-center">
           <p className="text-gray-600 text-sm">
             Already have an account?{' '}
             <Link href="/login" className="text-blue-500 hover:underline">
               Login here
             </Link>
           </p>
         </div>
       </div>
     </div>
   );
 }