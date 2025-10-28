 "use client";
 import { useAuth } from '../../components/AuthContext';
 import { useEffect, useState } from 'react';
 import OwnerFeedbackView from '@/app/components/OwnerFeedbackView';

 export default function OwnerDashboard() {
   const { user, loading, apiCall } = useAuth();
   const [restaurants, setRestaurants] = useState([]);
   const [selectedRestaurant, setSelectedRestaurant] = useState(null);
   const [menuItems, setMenuItems] = useState([]);
   const [showAddForm, setShowAddForm] = useState(false);
   const [showAddRestaurantForm, setShowAddRestaurantForm] = useState(false);
   const [activeTab, setActiveTab] = useState('restaurants');

   const [newItem, setNewItem] = useState({
     name: '',
     price: '',
     category: 'Veg',
     availability: true
   });

   const [newRestaurant, setNewRestaurant] = useState({
     name: '',
     address: '',
     openingTime: '09:00',
     closingTime: '22:00'
   });

   useEffect(() => {
     if (!loading && !user) {
       window.location.href = '/login';
       return;
     }

     if (user) {
       loadData();
     }
   }, [user, loading]);

   // ✅ LOAD ALL DATA - RESTAURANTS + THEIR DISHES
   const loadData = async () => {
     if (!user) return;

     try {
       await loadRealData();
     } catch (error) {
       loadDemoData();
     }
   };

   // ✅ LOAD REAL DATA WITH INDIVIDUAL RESTAURANT DISH COUNTS
   const loadRealData = async () => {
     try {
       // Fetch owner's restaurants
       const myRestaurants = await apiCall('/owner/restaurants');

       // ✅ LOAD DISHES FOR EACH RESTAURANT INDIVIDUALLY
       const restaurantsWithDishCounts = await Promise.all(
         myRestaurants.map(async (restaurant) => {
           try {
             const dishes = await apiCall(`/owner/restaurants/${restaurant.restaurantId}/dishes`);
             return {
               ...restaurant,
               dishCount: dishes.length
             };
           } catch (error) {
             return {
               ...restaurant,
               dishCount: 0
             };
           }
         })
       );

       setRestaurants(restaurantsWithDishCounts);

       if (restaurantsWithDishCounts.length === 0) {
         setSelectedRestaurant(null);
         setMenuItems([]);
         return;
       }

       // ✅ LOAD ALL DISHES FOR MENU MANAGEMENT
       const allDishes = [];
       for (const restaurant of myRestaurants) {
         try {
           const dishes = await apiCall(`/owner/restaurants/${restaurant.restaurantId}/dishes`);
           allDishes.push(...dishes.map(dish => ({
             ...dish,
             restaurantId: restaurant.restaurantId
           })));
         } catch (error) {
           console.log(`Error loading dishes for restaurant ${restaurant.name}`);
         }
       }
       setMenuItems(allDishes);

       // Auto-select first restaurant
       const firstRestaurant = restaurantsWithDishCounts[0];
       setSelectedRestaurant(firstRestaurant);

     } catch (error) {
       throw new Error('Failed to load real data');
     }
   };

   // ✅ LOAD MENU ITEMS FOR SPECIFIC RESTAURANT
   const loadRestaurantMenuItems = async (restaurantId) => {
     try {
       const menuItemsData = await apiCall(`/owner/restaurants/${restaurantId}/dishes`);

       // Update menuItems state with fresh data
       setMenuItems(prev => {
         const filtered = prev.filter(item => item.restaurantId !== restaurantId);
         const newItems = menuItemsData.map(item => ({
           ...item,
           restaurantId: restaurantId
         }));
         return [...filtered, ...newItems];
       });

       // ✅ UPDATE RESTAURANT DISH COUNT
       setRestaurants(prev =>
         prev.map(rest =>
           rest.restaurantId === restaurantId
             ? { ...rest, dishCount: menuItemsData.length }
             : rest
         )
       );

       return menuItemsData;
     } catch (error) {
       return [];
     }
   };

   // ✅ HANDLE RESTAURANT SELECTION - LOAD FRESH DATA
   const handleRestaurantSelect = async (restaurant) => {
     setSelectedRestaurant(restaurant);
     await loadRestaurantMenuItems(restaurant.restaurantId);
   };

   // ✅ GET DISH COUNT FOR A SPECIFIC RESTAURANT
   const getDishCount = (restaurantId) => {
     const restaurant = restaurants.find(r => r.restaurantId === restaurantId);
     return restaurant ? restaurant.dishCount : 0;
   };

   // ✅ GET MENU ITEMS FOR SELECTED RESTAURANT
   const getRestaurantMenuItems = () => {
     if (!selectedRestaurant) return [];
     return menuItems.filter(item => item.restaurantId === selectedRestaurant.restaurantId);
   };

   // ✅ ADD NEW RESTAURANT - WITH AUTO RELOAD
   const addRestaurant = async () => {
     if (!newRestaurant.name?.trim() || !newRestaurant.address?.trim()) {
       alert('Please provide restaurant name and address');
       return;
     }

     try {
       const restaurantData = {
         name: newRestaurant.name.trim(),
         address: newRestaurant.address.trim(),
         openingTime: newRestaurant.openingTime,
         closingTime: newRestaurant.closingTime
       };

       const newRest = await apiCall('/owner/restaurants', {
         method: 'POST',
         body: restaurantData
       });

       // ✅ ADD DISH COUNT TO NEW RESTAURANT
       const newRestaurantWithCount = {
         ...newRest,
         dishCount: 0
       };

       // ✅ RELOAD COMPLETE DATA AFTER ADDING RESTAURANT
       await loadData();

       setNewRestaurant({ name: '', address: '', openingTime: '09:00', closingTime: '22:00' });
       setShowAddRestaurantForm(false);

       alert('Restaurant added successfully!');

     } catch (error) {
       alert('Failed to save restaurant: ' + error.message);
     }
   };

   // ✅ ADD NEW MENU ITEM - WITH AUTO COUNT UPDATE
   const addMenuItem = async () => {
     if (!newItem.name?.trim() || !newItem.price) {
       alert('Please provide dish name and price');
       return;
     }

     if (!selectedRestaurant) {
       alert('Please select a restaurant first');
       return;
     }

     try {
       const itemData = {
         name: newItem.name.trim(),
         price: parseFloat(newItem.price),
         category: newItem.category,
         availability: newItem.availability
       };

       const newDish = await apiCall(`/owner/restaurants/${selectedRestaurant.restaurantId}/dishes`, {
         method: 'POST',
         body: itemData
       });

       // ✅ UPDATE DISH COUNT FOR THE SPECIFIC RESTAURANT
       setRestaurants(prev =>
         prev.map(rest =>
           rest.restaurantId === selectedRestaurant.restaurantId
             ? { ...rest, dishCount: rest.dishCount + 1 }
             : rest
         )
       );

       // ✅ RELOAD MENU ITEMS AFTER ADDING DISH
       await loadRestaurantMenuItems(selectedRestaurant.restaurantId);

       setNewItem({ name: '', price: '', category: 'Veg', availability: true });
       setShowAddForm(false);

       alert('Dish added successfully!');

     } catch (error) {
       alert('Failed to save dish: ' + error.message);
     }
   };

   // ✅ TOGGLE DISH AVAILABILITY
   const toggleAvailability = async (dishId) => {
     try {
       const dish = menuItems.find(item => item.dishId === dishId);
       if (!dish) return;

       const updatedDish = { ...dish, availability: !dish.availability };

       await apiCall(`/owner/dishes/${dishId}`, {
         method: 'PUT',
         body: updatedDish
       });

       // Update local state
       setMenuItems(menuItems.map(item =>
         item.dishId === dishId ? { ...item, availability: !item.availability } : item
       ));

     } catch (error) {
       alert('Failed to update dish availability: ' + error.message);
     }
   };

   // ✅ DELETE MENU ITEM - WITH COUNT UPDATE
   const deleteMenuItem = async (dishId) => {
     if (!confirm('Are you sure you want to delete this menu item?')) {
       return;
     }

     try {
       const dishToDelete = menuItems.find(item => item.dishId === dishId);

       await apiCall(`/owner/dishes/${dishId}`, {
         method: 'DELETE'
       });

       // ✅ UPDATE DISH COUNT FOR THE RESTAURANT
       if (dishToDelete && selectedRestaurant) {
         setRestaurants(prev =>
           prev.map(rest =>
             rest.restaurantId === selectedRestaurant.restaurantId
               ? { ...rest, dishCount: Math.max(0, rest.dishCount - 1) }
               : rest
           )
         );
       }

       // ✅ RELOAD DATA AFTER DELETION
       if (selectedRestaurant) {
         await loadRestaurantMenuItems(selectedRestaurant.restaurantId);
       }

       alert('Dish deleted successfully!');

     } catch (error) {
       alert('Failed to delete dish: ' + error.message);
     }
   };

   // 🎭 DEMO DATA (Fallback)
   const loadDemoData = () => {
     const demoRestaurants = [
       {
         restaurantId: 1,
         name: 'Spice Garden',
         address: 'MG Road, Delhi',
         openingTime: '10:00',
         closingTime: '23:00',
         status: 'Approved',
         dishCount: 1
       },
       {
         restaurantId: 2,
         name: 'Pizza Palace',
         address: 'Connaught Place, Delhi',
         openingTime: '11:00',
         closingTime: '22:00',
         status: 'Approved',
         dishCount: 0
       }
     ];

     const demoMenuItems = [
       {
         dishId: 1,
         name: 'Butter Chicken',
         price: 180,
         category: 'Non-Veg',
         availability: true,
         restaurantId: 1
       }
     ];

     setRestaurants(demoRestaurants);
     setMenuItems(demoMenuItems);

     if (demoRestaurants.length > 0) {
       setSelectedRestaurant(demoRestaurants[0]);
     }
   };

   // ✅ CALCULATE TOTAL DISHES ACROSS ALL RESTAURANTS
   const getTotalDishes = () => {
     return restaurants.reduce((total, restaurant) => total + restaurant.dishCount, 0);
   };

   // ⏳ LOADING STATE
   if (loading) {
     return (
       <div className="min-h-screen flex items-center justify-center">
         <div className="flex flex-col items-center">
           <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mb-4"></div>
           <p className="text-gray-600">Loading dashboard...</p>
         </div>
       </div>
     );
   }

   if (!user) {
     return null;
   }

   return (
     <div className="min-h-screen bg-gray-100">
       <div className="max-w-7xl mx-auto py-6 px-4">
         {/* 👋 WELCOME SECTION */}
         <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
           <h1 className="text-3xl font-bold text-gray-900 mb-2">
             Owner Dashboard 🏪
           </h1>
           <p className="text-gray-700">
             Welcome back, {user.name}! Manage your restaurants and menu.
           </p>
           {/* ✅ TOTAL RESTAURANTS & DISHES COUNT */}
           <div className="mt-4 flex gap-4 text-sm">
             <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded">
               🏪 Total Restaurants: {restaurants.length}
             </span>
             <span className="bg-green-100 text-green-800 px-3 py-1 rounded">
               🍽️ Total Dishes: {getTotalDishes()}
             </span>
           </div>
         </div>

         {/* 🗂️ TAB NAVIGATION */}
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
                 🏪 My Restaurants ({restaurants.length})
               </button>
               <button
                 onClick={() => setActiveTab('menu')}
                 className={`py-4 px-6 text-center border-b-2 font-medium text-sm ${
                   activeTab === 'menu'
                     ? 'border-blue-500 text-blue-600'
                     : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                 }`}
               >
                 🍽️ Menu Management
               </button>
               <button
                 onClick={() => setActiveTab('feedback')}
                 className={`py-4 px-6 text-center border-b-2 font-medium text-sm ${
                   activeTab === 'feedback'
                     ? 'border-blue-500 text-blue-600'
                     : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                 }`}
               >
                 ⭐ Customer Feedback
               </button>
             </nav>
           </div>

           <div className="p-6">
             {/* 🏪 RESTAURANTS TAB */}
             {activeTab === 'restaurants' && (
               <>
                 {restaurants.length === 0 && (
                   <div className="bg-white p-8 rounded-lg shadow text-center border border-gray-200">
                     <div className="text-6xl mb-4">🏪</div>
                     <h2 className="text-2xl font-bold text-gray-900 mb-2">
                       No Restaurants Yet
                     </h2>
                     <p className="text-gray-700 mb-6">
                       Start by adding your first restaurant to manage menus and orders.
                     </p>
                     <button
                       onClick={() => setShowAddRestaurantForm(true)}
                       className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 text-lg font-medium transition-colors"
                     >
                       + Add Your First Restaurant
                     </button>
                   </div>
                 )}

                 {restaurants.length > 0 && (
                   <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                     {/* 🏪 RESTAURANTS PANEL */}
                     <div className="bg-white p-6 rounded-lg shadow">
                       <div className="flex justify-between items-center mb-4">
                         <h2 className="text-xl font-semibold text-gray-900">
                           🏪 My Restaurants ({restaurants.length})
                         </h2>
                         <button
                           onClick={() => setShowAddRestaurantForm(true)}
                           className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition-colors"
                         >
                           + Add Restaurant
                         </button>
                       </div>

                       <div className="space-y-3">
                         {restaurants.map((restaurant) => (
                           <div
                             key={restaurant.restaurantId}
                             className={`border p-4 rounded-lg cursor-pointer transition-colors ${
                               selectedRestaurant?.restaurantId === restaurant.restaurantId
                                 ? 'border-blue-500 bg-blue-50'
                                 : 'border-gray-200 hover:bg-gray-50'
                             }`}
                             onClick={() => handleRestaurantSelect(restaurant)}
                           >
                             <div className="flex justify-between items-start">
                               <div className="flex-1">
                                 <h3 className="font-semibold text-lg text-gray-900">{restaurant.name}</h3>
                                 <p className="text-sm text-gray-700">{restaurant.address}</p>
                                 <p className="text-sm text-gray-700">
                                   Timing: {restaurant.openingTime} - {restaurant.closingTime}
                                 </p>
                                 <p className={`text-sm font-medium ${
                                   restaurant.status === 'Approved' ? 'text-green-700' :
                                   restaurant.status === 'Pending' ? 'text-yellow-700' : 'text-red-700'
                                 }`}>
                                   Status: {restaurant.status}
                                 </p>
                               </div>
                               {/* ✅ INDIVIDUAL RESTAURANT DISH COUNT */}
                               <div className="text-right">
                                 <div className="bg-purple-100 text-purple-800 px-3 py-2 rounded-lg text-center min-w-20 border border-purple-200">
                                   <div className="text-xl font-bold">{restaurant.dishCount}</div>
                                   <div className="text-xs font-medium">Dishes</div>
                                 </div>
                               </div>
                             </div>
                           </div>
                         ))}
                       </div>
                     </div>

                     {/* 🍽️ MENU MANAGEMENT PANEL */}
                     <div className="bg-white p-6 rounded-lg shadow">
                       <div className="flex justify-between items-center mb-4">
                         <h2 className="text-xl font-semibold text-gray-900">
                           🍽️ Menu Management
                         </h2>
                         {selectedRestaurant && (
                           <button
                             onClick={() => setShowAddForm(true)}
                             className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 transition-colors"
                           >
                             + Add Dish
                           </button>
                         )}
                       </div>

                       {selectedRestaurant ? (
                         <div>
                           <div className="flex justify-between items-center mb-4">
                             <p className="text-gray-700 font-medium">
                               Managing menu for: <strong className="text-gray-900">{selectedRestaurant.name}</strong>
                             </p>
                             {/* ✅ SELECTED RESTAURANT DISH COUNT */}
                             <span className="bg-purple-100 text-purple-800 px-3 py-1 rounded text-sm font-medium border border-purple-200">
                               {selectedRestaurant.dishCount} dishes
                             </span>
                           </div>

                           <div className="space-y-3">
                             {getRestaurantMenuItems().map((item) => (
                               <div
                                 key={item.dishId}
                                 className="border border-gray-300 p-3 rounded-lg flex justify-between items-center hover:shadow-sm transition-shadow bg-white"
                               >
                                 <div>
                                   <h4 className="font-medium text-gray-900">{item.name}</h4>
                                   <p className="text-sm text-gray-700">
                                     ₹{item.price} • {item.category}
                                   </p>
                                   <span className={`text-xs px-2 py-1 rounded font-medium ${
                                     item.availability ? 'bg-green-100 text-green-800 border border-green-200' : 'bg-red-100 text-red-800 border border-red-200'
                                   }`}>
                                     {item.availability ? 'Available' : 'Not Available'}
                                   </span>
                                 </div>
                                 <div className="flex gap-2">
                                   <button
                                     onClick={() => toggleAvailability(item.dishId)}
                                     className="text-blue-600 hover:text-blue-800 text-sm font-medium transition-colors"
                                   >
                                     {item.availability ? 'Disable' : 'Enable'}
                                   </button>
                                   <button
                                     onClick={() => deleteMenuItem(item.dishId)}
                                     className="text-red-600 hover:text-red-800 text-sm font-medium transition-colors"
                                   >
                                     Delete
                                   </button>
                                 </div>
                               </div>
                             ))}

                             {getRestaurantMenuItems().length === 0 && (
                               <div className="text-center py-8 text-gray-600 bg-gray-50 rounded-lg border border-gray-200">
                                 <div className="text-4xl mb-2">🍽️</div>
                                 <p className="text-gray-700">No dishes yet for this restaurant</p>
                                 <p className="text-sm mt-1 text-gray-600">Add your first dish to get started</p>
                               </div>
                             )}
                           </div>
                         </div>
                       ) : (
                         <div className="text-center py-8 text-gray-600 bg-gray-50 rounded-lg border border-gray-200">
                           <div className="text-4xl mb-2">🏪</div>
                           <p className="text-gray-700">Select a restaurant to manage its menu</p>
                         </div>
                       )}
                     </div>
                   </div>
                 )}
               </>
             )}

             {/* 🍽️ MENU MANAGEMENT TAB */}
             {activeTab === 'menu' && (
               <div>
                 <h2 className="text-xl font-semibold text-gray-900 mb-4">🍽️ Menu Management</h2>
                 {selectedRestaurant ? (
                   <div>
                     <div className="flex justify-between items-center mb-4">
                       <p className="text-gray-700 font-medium">
                         Managing menu for: <strong className="text-gray-900">{selectedRestaurant.name}</strong>
                       </p>
                       <div className="flex gap-4 items-center">
                         <span className="bg-purple-100 text-purple-800 px-3 py-1 rounded text-sm font-medium border border-purple-200">
                           {selectedRestaurant.dishCount} dishes
                         </span>
                         <button
                           onClick={() => setShowAddForm(true)}
                           className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition-colors font-medium"
                         >
                           + Add Dish
                         </button>
                       </div>
                     </div>

                     <div className="space-y-3">
                       {getRestaurantMenuItems().map((item) => (
                         <div
                           key={item.dishId}
                           className="border border-gray-300 p-3 rounded-lg flex justify-between items-center hover:shadow-sm transition-shadow bg-white"
                         >
                           <div>
                             <h4 className="font-medium text-gray-900">{item.name}</h4>
                             <p className="text-sm text-gray-700">
                               ₹{item.price} • {item.category}
                             </p>
                             <span className={`text-xs px-2 py-1 rounded font-medium ${
                               item.availability ? 'bg-green-100 text-green-800 border border-green-200' : 'bg-red-100 text-red-800 border border-red-200'
                             }`}>
                               {item.availability ? 'Available' : 'Not Available'}
                             </span>
                           </div>
                           <div className="flex gap-2">
                             <button
                               onClick={() => toggleAvailability(item.dishId)}
                               className="text-blue-600 hover:text-blue-800 text-sm font-medium transition-colors"
                             >
                               {item.availability ? 'Disable' : 'Enable'}
                             </button>
                             <button
                               onClick={() => deleteMenuItem(item.dishId)}
                               className="text-red-600 hover:text-red-800 text-sm font-medium transition-colors"
                             >
                               Delete
                             </button>
                           </div>
                         </div>
                       ))}

                       {getRestaurantMenuItems().length === 0 && (
                         <div className="text-center py-8 text-gray-600 bg-gray-50 rounded-lg border border-gray-200">
                           <div className="text-4xl mb-2">🍽️</div>
                           <p className="text-gray-700">No dishes yet for this restaurant</p>
                           <p className="text-sm mt-1 text-gray-600">Add your first dish to get started</p>
                         </div>
                       )}
                     </div>
                   </div>
                 ) : (
                   <div className="text-center py-8 text-gray-600 bg-gray-50 rounded-lg border border-gray-200">
                     <div className="text-4xl mb-2">🏪</div>
                     <p className="text-gray-700">Please select a restaurant from the Restaurants tab first</p>
                   </div>
                 )}
               </div>
             )}

             {/* ⭐ CUSTOMER FEEDBACK TAB */}
             {activeTab === 'feedback' && (
               <OwnerFeedbackView />
             )}
           </div>
         </div>

         {/* ➕ ADD RESTAURANT MODAL */}
         {showAddRestaurantForm && (
           <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
             <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
               <h3 className="text-xl font-bold mb-4">Add New Restaurant</h3>
               <div className="space-y-4">
                 <input
                   type="text"
                   placeholder="Restaurant Name *"
                   className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   value={newRestaurant.name}
                   onChange={(e) => setNewRestaurant({...newRestaurant, name: e.target.value})}
                 />
                 <textarea
                   placeholder="Restaurant Address *"
                   className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   rows="3"
                   value={newRestaurant.address}
                   onChange={(e) => setNewRestaurant({...newRestaurant, address: e.target.value})}
                 />
                 <div className="flex gap-4">
                   <div className="flex-1">
                     <label className="block text-sm text-gray-600 mb-1">Opening Time</label>
                     <input
                       type="time"
                       className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                       value={newRestaurant.openingTime}
                       onChange={(e) => setNewRestaurant({...newRestaurant, openingTime: e.target.value})}
                     />
                   </div>
                   <div className="flex-1">
                     <label className="block text-sm text-gray-600 mb-1">Closing Time</label>
                     <input
                       type="time"
                       className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                       value={newRestaurant.closingTime}
                       onChange={(e) => setNewRestaurant({...newRestaurant, closingTime: e.target.value})}
                     />
                   </div>
                 </div>
                 <div className="flex gap-2">
                   <button
                     onClick={addRestaurant}
                     className="bg-green-500 text-white px-4 py-2 rounded flex-1 hover:bg-green-600 transition-colors"
                   >
                     Save Restaurant
                   </button>
                   <button
                     onClick={() => setShowAddRestaurantForm(false)}
                     className="bg-gray-500 text-white px-4 py-2 rounded flex-1 hover:bg-gray-600 transition-colors"
                   >
                     Cancel
                   </button>
                 </div>
               </div>
             </div>
           </div>
         )}

         {/* ➕ ADD DISH MODAL */}
         {showAddForm && (
           <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
             <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
               <h3 className="text-xl font-bold mb-4">Add New Dish</h3>
               <div className="space-y-4">
                 <input
                   type="text"
                   placeholder="Dish Name *"
                   className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   value={newItem.name}
                   onChange={(e) => setNewItem({...newItem, name: e.target.value})}
                 />
                 <input
                   type="number"
                   placeholder="Price *"
                   className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   value={newItem.price}
                   onChange={(e) => setNewItem({...newItem, price: e.target.value})}
                   min="1"
                 />
                 <select
                   className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                   value={newItem.category}
                   onChange={(e) => setNewItem({...newItem, category: e.target.value})}
                 >
                   <option value="Veg">Veg</option>
                   <option value="Non-Veg">Non-Veg</option>
                   <option value="Drinks">Drinks</option>
                 </select>
                 <div className="flex items-center">
                   <input
                     type="checkbox"
                     checked={newItem.availability}
                     onChange={(e) => setNewItem({...newItem, availability: e.target.checked})}
                     className="mr-2 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                   />
                   <label className="text-gray-700">Available for order</label>
                 </div>
                 <div className="flex gap-2">
                   <button
                     onClick={addMenuItem}
                     className="bg-green-500 text-white px-4 py-2 rounded flex-1 hover:bg-green-600 transition-colors"
                   >
                     Save Dish
                   </button>
                   <button
                     onClick={() => setShowAddForm(false)}
                     className="bg-gray-500 text-white px-4 py-2 rounded flex-1 hover:bg-gray-600 transition-colors"
                   >
                     Cancel
                   </button>
                 </div>
               </div>
             </div>
           </div>
         )}
       </div>
     </div>
   );
 }