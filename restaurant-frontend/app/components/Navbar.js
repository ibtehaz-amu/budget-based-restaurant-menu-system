 "use client";
 import Link from 'next/link';
 import { useAuth } from './AuthContext';

 export default function Navbar() {
   const { user, logout } = useAuth();

   const handleLogout = () => {
     logout();
     window.location.href = '/';
   };

   return (
     <nav className="bg-blue-600 text-white p-4 shadow-lg">
       <div className="max-w-7xl mx-auto flex justify-between items-center">
         <Link href="/" className="text-xl font-bold flex items-center gap-2">
           🍽️ Budget Restaurant Menu
         </Link>

         <div className="flex items-center gap-4">
           {user ? (
             <div className="flex items-center gap-3">
               <span className="text-sm">Hello, <strong>{user.name}</strong></span>
               <button
                 onClick={handleLogout}
                 className="bg-red-500 hover:bg-red-600 px-3 py-1 rounded text-sm transition"
               >
                 Logout
               </button>
             </div>
           ) : (
             <div className="flex gap-3">
               <Link
                 href="/login"
                 className="bg-green-500 hover:bg-green-600 px-4 py-2 rounded transition"
               >
                 Login
               </Link>
               <Link
                 href="/register"
                 className="bg-purple-500 hover:bg-purple-600 px-4 py-2 rounded transition"
               >
                 Register
               </Link>
             </div>
           )}
         </div>
       </div>
     </nav>
   );
 }