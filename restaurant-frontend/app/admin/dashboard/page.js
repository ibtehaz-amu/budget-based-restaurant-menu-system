 "use client";
 import { useAuth } from '../../components/AuthContext';
 import { useEffect, useState } from 'react';

 export default function AdminDashboard() {
   const { user, loading, apiCall } = useAuth();
   const [pendingRestaurants, setPendingRestaurants] = useState([]);
   const [users, setUsers] = useState([]);
   const [activeTab, setActiveTab] = useState('restaurants');

   useEffect(() => {
     if (!loading && !user) {
       window.location.href = '/login';
     }
   }, [user, loading]);

   // Load pending restaurants from database
   const loadPendingRestaurants = async () => {
     try {
       const data = await apiCall('/admin/restaurants/pending');
       setPendingRestaurants(data);
     } catch (error) {
       console.error('Error loading pending restaurants:', error);
       setPendingRestaurants([]);
     }
   };

   // Load users from database
   const loadUsers = async () => {
     try {
       const data = await apiCall('/admin/users');
       setUsers(data);
     } catch (error) {
       console.error('Error loading users:', error);
       setUsers([]);
     }
   };

   useEffect(() => {
     if (user) {
       loadPendingRestaurants();
       loadUsers();
     }
   }, [user]);

   const approveRestaurant = async (restaurantId) => {
     try {
       await apiCall(`/admin/restaurants/${restaurantId}/approve`, {
         method: 'POST'
       });

       setPendingRestaurants(pendingRestaurants.filter(rest => rest.restaurantId !== restaurantId));
       alert('Restaurant approved successfully!');
     } catch (error) {
       alert('Error approving restaurant: ' + error.message);
     }
   };

   const rejectRestaurant = async (restaurantId) => {
     try {
       await apiCall(`/admin/restaurants/${restaurantId}/reject`, {
         method: 'POST'
       });

       setPendingRestaurants(pendingRestaurants.filter(rest => rest.restaurantId !== restaurantId));
       alert('Restaurant rejected successfully!');
     } catch (error) {
       alert('Error rejecting restaurant: ' + error.message);
     }
   };

   // ✅ DELETE USER FUNCTION
   const deleteUser = async (userId, userName) => {
     if (!confirm(`Are you sure you want to delete user: ${userName}?`)) {
       return;
     }

     try {
       const response = await apiCall(`/admin/users/${userId}`, {
         method: 'DELETE'
       });

       // Update users list
       setUsers(users.filter(user => user.userId !== userId));
       alert(`User "${userName}" deleted successfully!`);

     } catch (error) {
       if (error.message.includes('Cannot delete administrator')) {
         alert('❌ Cannot delete administrator account!');
       } else {
         alert('Error deleting user: ' + error.message);
       }
     }
   };

   // ✅ Filter users by role
   const getStudents = () => {
     return users.filter(user => user.role === 'STUDENT');
   };

   const getOwners = () => {
     return users.filter(user => user.role === 'OWNER');
   };

   const getAdmins = () => {
     return users.filter(user => user.role === 'ADMIN');
   };

   if (loading) {
     return (
       <div className="min-h-screen flex items-center justify-center">
         <div className="text-xl">Loading...</div>
       </div>
     );
   }

   if (!user) {
     return null;
   }

   return (
     <div className="min-h-screen bg-gray-100">
       <div className="max-w-7xl mx-auto py-6 px-4">
         <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
           <h1 className="text-3xl font-bold text-gray-900 mb-2">
             Admin Dashboard 👨‍💼
           </h1>
           <p className="text-gray-600">
             Manage restaurants, users, and system settings
           </p>
         </div>

         {/* ✅ Tab Navigation */}
         <div className="bg-white rounded-lg shadow-sm mb-6">
           <div className="border-b border-gray-200">
             <nav className="flex -mb-px">
               <button
                 onClick={() => setActiveTab('restaurants')}
                 className={`py-4 px-6 text-center border-b-2 font-medium text-sm ${
                   activeTab === 'restaurants'
                     ? 'border-blue-500 text-blue-600'
                     : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                 }`}
               >
                 ⏳ Pending Restaurants ({pendingRestaurants.length})
               </button>
               <button
                 onClick={() => setActiveTab('owners')}
                 className={`py-4 px-6 text-center border-b-2 font-medium text-sm ${
                   activeTab === 'owners'
                     ? 'border-blue-500 text-blue-600'
                     : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                 }`}
               >
                 🏪 Restaurant Owners ({getOwners().length})
               </button>
               <button
                 onClick={() => setActiveTab('students')}
                 className={`py-4 px-6 text-center border-b-2 font-medium text-sm ${
                   activeTab === 'students'
                     ? 'border-blue-500 text-blue-600'
                     : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                 }`}
               >
                 🎓 Students ({getStudents().length})
               </button>
               <button
                 onClick={() => setActiveTab('admins')}
                 className={`py-4 px-6 text-center border-b-2 font-medium text-sm ${
                   activeTab === 'admins'
                     ? 'border-blue-500 text-blue-600'
                     : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                 }`}
               >
                 👨‍💼 Admins ({getAdmins().length})
               </button>
             </nav>
           </div>

           <div className="p-6">
             {/* PENDING RESTAURANTS TAB */}
             {activeTab === 'restaurants' && (
               <div>
                 <h2 className="text-xl font-semibold mb-4">
                   ⏳ Pending Restaurant Approvals
                 </h2>
                 <div className="space-y-3">
                   {pendingRestaurants.map(restaurant => (
                     <div key={restaurant.restaurantId} className="border p-4 rounded-lg">
                       <h3 className="font-semibold text-lg">{restaurant.name}</h3>
                       <p className="text-sm text-gray-600">Owner: {restaurant.owner?.name}</p>
                       <p className="text-sm text-gray-600">Address: {restaurant.address}</p>
                       <p className="text-sm text-gray-600">Applied: {new Date().toLocaleDateString()}</p>
                       <div className="mt-2 flex gap-2">
                         <button
                           onClick={() => approveRestaurant(restaurant.restaurantId)}
                           className="bg-green-500 text-white px-3 py-1 rounded text-sm hover:bg-green-600"
                         >
                           Approve
                         </button>
                         <button
                           onClick={() => rejectRestaurant(restaurant.restaurantId)}
                           className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
                         >
                           Reject
                         </button>
                       </div>
                     </div>
                   ))}
                   {pendingRestaurants.length === 0 && (
                     <p className="text-center text-gray-500 py-4">No pending restaurants</p>
                   )}
                 </div>
               </div>
             )}

             {/* RESTAURANT OWNERS TAB */}
             {activeTab === 'owners' && (
               <div>
                 <h2 className="text-xl font-semibold mb-4">
                   🏪 Restaurant Owners Management ({getOwners().length})
                 </h2>
                 <div className="space-y-3">
                   {getOwners().map(userItem => (
                     <div key={userItem.userId} className="border p-3 rounded-lg flex justify-between items-center">
                       <div>
                         <h4 className="font-medium">{userItem.name}</h4>
                         <p className="text-sm text-gray-600">{userItem.email}</p>
                         <p className="text-sm text-gray-600">Phone: {userItem.phoneNumber || 'Not provided'}</p>
                         <span className="text-xs px-2 py-1 rounded bg-green-100 text-green-800">
                           OWNER
                         </span>
                       </div>
                       <button
                         onClick={() => deleteUser(userItem.userId, userItem.name)}
                         className="text-red-500 text-sm hover:text-red-700"
                       >
                         Delete
                       </button>
                     </div>
                   ))}
                   {getOwners().length === 0 && (
                     <p className="text-center text-gray-500 py-4">No restaurant owners registered</p>
                   )}
                 </div>
               </div>
             )}

             {/* STUDENTS TAB */}
             {activeTab === 'students' && (
               <div>
                 <h2 className="text-xl font-semibold mb-4">
                   🎓 Students Management ({getStudents().length})
                 </h2>
                 <div className="space-y-3">
                   {getStudents().map(userItem => (
                     <div key={userItem.userId} className="border p-3 rounded-lg flex justify-between items-center">
                       <div>
                         <h4 className="font-medium">{userItem.name}</h4>
                         <p className="text-sm text-gray-600">{userItem.email}</p>
                         <p className="text-sm text-gray-600">Phone: {userItem.phoneNumber || 'Not provided'}</p>
                         <span className="text-xs px-2 py-1 rounded bg-blue-100 text-blue-800">
                           STUDENT
                         </span>
                       </div>
                       <button
                         onClick={() => deleteUser(userItem.userId, userItem.name)}
                         className="text-red-500 text-sm hover:text-red-700"
                       >
                         Delete
                       </button>
                     </div>
                   ))}
                   {getStudents().length === 0 && (
                     <p className="text-center text-gray-500 py-4">No students registered</p>
                   )}
                 </div>
               </div>
             )}

             {/* ADMINS TAB */}
             {activeTab === 'admins' && (
               <div>
                 <h2 className="text-xl font-semibold mb-4">
                   👨‍💼 System Administrators ({getAdmins().length})
                 </h2>
                 <div className="space-y-3">
                   {getAdmins().map(userItem => (
                     <div key={userItem.userId} className="border p-3 rounded-lg flex justify-between items-center">
                       <div>
                         <h4 className="font-medium">{userItem.name}</h4>
                         <p className="text-sm text-gray-600">{userItem.email}</p>
                         <span className="text-xs px-2 py-1 rounded bg-purple-100 text-purple-800">
                           ADMIN
                         </span>
                       </div>
                       <button
                         disabled={true}
                         className="text-gray-400 text-sm cursor-not-allowed"
                         title="Cannot delete administrator accounts"
                       >
                         Protected
                       </button>
                     </div>
                   ))}
                 </div>
               </div>
             )}
           </div>
         </div>
       </div>
     </div>
   );
 }