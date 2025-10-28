 "use client";
 import { AuthProvider } from './components/AuthContext';
 import Navbar from './components/Navbar';
 import './globals.css';

 export default function RootLayout({ children }) {
   return (
     <html lang="en">
       <body className="bg-gray-100">
         <AuthProvider>
           <Navbar />
           <main>{children}</main>
         </AuthProvider>
       </body>
     </html>
   );
 }