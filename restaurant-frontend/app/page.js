 "use client";
 import { useAuth } from './components/AuthContext';
 import Link from 'next/link';

 export default function Home() {
   const { user } = useAuth();

   return (
     <div className="min-h-screen bg-gray-50">
       {/* Hero Section */}
       <div className="bg-gradient-to-r from-blue-500 to-indigo-500 text-white">
         <div className="max-w-7xl mx-auto py-16 px-4 text-center">
           <h1 className="text-4xl font-bold mb-4">
             🍽️ Budget Based Restaurant Menu
           </h1>
           <p className="text-xl mb-8 opacity-90">
             Find delicious food within your budget. Perfect for students!
           </p>

           {user ? (
             <div className="space-y-4">
               <p className="text-lg font-medium">Welcome back, {user.name}! 🎉</p>
               <div className="flex justify-center gap-4">
                 {user.role === 'STUDENT' && (
                   <Link
                     href="/student/dashboard"
                     className="bg-white text-blue-600 font-semibold px-6 py-3 rounded-lg hover:bg-gray-100 transition"
                   >
                     Go to Dashboard
                   </Link>
                 )}
                 {user.role === 'OWNER' && (
                   <Link
                     href="/owner/dashboard"
                     className="bg-white text-green-600 font-semibold px-6 py-3 rounded-lg hover:bg-gray-100 transition"
                   >
                     Manage Restaurant
                   </Link>
                 )}
               </div>
             </div>
           ) : (
             <div className="space-y-4">
               <p className="text-lg font-medium">Get started by creating an account</p>
               <div className="flex justify-center gap-4">
                 <Link
                   href="/register"
                   className="bg-white text-blue-700 font-semibold px-6 py-3 rounded-lg hover:bg-gray-100 transition inline-block"
                 >
                   Create Account
                 </Link>
                 <Link
                   href="/login"
                   className="bg-white text-green-700 font-semibold px-6 py-3 rounded-lg hover:bg-gray-100 transition inline-block"
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
         <h2 className="text-3xl font-bold text-center text-gray-800 mb-10">
           🔥 Key Features
         </h2>

         <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
           <div className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition text-center border border-gray-200">
             <div className="text-3xl mb-4">💰</div>
             <h3 className="text-xl font-semibold mb-2 text-gray-800">Budget Search</h3>
             <p className="text-gray-600">Find dishes within your specified budget</p>
           </div>

           <div className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition text-center border border-gray-200">
             <div className="text-3xl mb-4">🍕</div>
             <h3 className="text-xl font-semibold mb-2 text-gray-800">Multiple Categories</h3>
             <p className="text-gray-600">Veg, Non-Veg, Drinks and more</p>
           </div>

           <div className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition text-center border border-gray-200">
             <div className="text-3xl mb-4">⏰</div>
             <h3 className="text-xl font-semibold mb-2 text-gray-800">Time-based</h3>
             <p className="text-gray-600">Find food available at your preferred time</p>
           </div>
         </div>

         {/* For Restaurant Owners */}
         <div className="mt-16 bg-blue-100 border border-blue-200 p-8 rounded-lg text-center shadow-sm">
           <h2 className="text-2xl font-bold text-blue-900 mb-4">
             For Restaurant Owners
           </h2>
           <p className="text-blue-800 mb-6">
             Join our platform to reach more students and grow your business
           </p>
           <Link
             href="/register"
             className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition inline-block"
           >
             Register Your Restaurant
           </Link>
         </div>
       </div>
     </div>
   );
 }
