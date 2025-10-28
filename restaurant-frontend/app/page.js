 "use client";
 import { useAuth } from './components/AuthContext';
 import Link from 'next/link';

 export default function Home() {
   const { user } = useAuth();

   return (
     <div className="min-h-screen bg-gray-100">
       {/* Hero Section */}
       <div className="bg-white">
         <div className="max-w-7xl mx-auto py-16 px-4 text-center">
           <h1 className="text-4xl font-bold text-gray-900 mb-4">
             🍽️ Budget Based Restaurant Menu
           </h1>
           <p className="text-xl text-gray-600 mb-8">
             Find delicious food within your budget. Perfect for students!
           </p>

           {user ? (
             <div className="space-y-4">
               <p className="text-lg">Welcome back, {user.name}! 🎉</p>
               <div className="flex justify-center gap-4">
                 {user.role === 'STUDENT' && (
                   <Link
                     href="/student/dashboard"
                     className="bg-blue-500 text-white px-6 py-3 rounded-lg hover:bg-blue-600 transition"
                   >
                     Go to Dashboard
                   </Link>
                 )}
                 {user.role === 'OWNER' && (
                   <Link
                     href="/owner/dashboard"
                     className="bg-green-500 text-white px-6 py-3 rounded-lg hover:bg-green-600 transition"
                   >
                     Manage Restaurant
                   </Link>
                 )}
               </div>
             </div>
           ) : (
             <div className="space-y-4">
               <p className="text-lg">Get started by creating an account</p>
               <div className="flex justify-center gap-4">
                 <Link
                   href="/register"
                   className="bg-blue-500 text-white px-6 py-3 rounded-lg hover:bg-blue-600 transition inline-block"
                 >
                   Create Account
                 </Link>
                 <Link
                   href="/login"
                   className="bg-green-500 text-white px-6 py-3 rounded-lg hover:bg-green-600 transition inline-block"
                 >
                   Login
                 </Link>
               </div>
             </div>
           )}
         </div>
       </div>

       {/* Features Section */}
       <div className="max-w-7xl mx-auto py-12 px-4">
         <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
           <div className="bg-white p-6 rounded-lg shadow text-center">
             <div className="text-2xl mb-4">💰</div>
             <h3 className="text-xl font-semibold mb-2">Budget Search</h3>
             <p className="text-gray-600">Find dishes within your specified budget</p>
           </div>

           <div className="bg-white p-6 rounded-lg shadow text-center">
             <div className="text-2xl mb-4">🍕</div>
             <h3 className="text-xl font-semibold mb-2">Multiple Categories</h3>
             <p className="text-gray-600">Veg, Non-Veg, Drinks and more</p>
           </div>

           <div className="bg-white p-6 rounded-lg shadow text-center">
             <div className="text-2xl mb-4">⏰</div>
             <h3 className="text-xl font-semibold mb-2">Time-based</h3>
             <p className="text-gray-600">Find food available at your preferred time</p>
           </div>
         </div>

         {/* For Restaurant Owners */}
         <div className="mt-12 bg-blue-50 p-8 rounded-lg text-center">
           <h2 className="text-2xl font-bold text-blue-900 mb-4">For Restaurant Owners</h2>
           <p className="text-blue-700 mb-6">
             Join our platform to reach more students and grow your business
           </p>
           <Link
             href="/register"
             className="bg-blue-500 text-white px-6 py-3 rounded-lg hover:bg-blue-600 transition inline-block"
           >
             Register Your Restaurant
           </Link>
         </div>
       </div>
     </div>
   );
 }