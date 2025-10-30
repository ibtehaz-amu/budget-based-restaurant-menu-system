 "use client";
  import { useAuth } from '../../components/AuthContext';
  import { useEffect, useState } from 'react';
  import FeedbackButton from '@/app/components/FeedbackButton';

  // ✅ UPDATED API URL
  const API_BASE_URL = 'https://budget-based-restaurant-menu-system.onrender.com';

  export default function StudentDashboard() {
    const { user, loading } = useAuth();
    const [budget, setBudget] = useState('');
    const [dishes, setDishes] = useState([]);
    const [searchLoading, setSearchLoading] = useState(false);
    const [searchError, setSearchError] = useState('');
    const [category, setCategory] = useState('');
    const [timing, setTiming] = useState('');

    useEffect(() => {
      if (loading) return;
      if (!user) {
        window.location.href = '/login';
      }
    }, [user, loading]);

    // ✅ Category grouping
    const groupDishesByCategory = (dishes) => {
      const grouped = {};
      dishes.forEach(dish => {
        const cat = dish.category || 'Other';
        if (!grouped[cat]) grouped[cat] = [];
        grouped[cat].push(dish);
      });
      return grouped;
    };

    // ✅ Category config
    const getCategoryInfo = (category) => {
      const config = {
        'Veg': { icon: '🥗', color: 'bg-green-100', textColor: 'text-green-800', borderColor: 'border-green-200' },
        'Non-Veg': { icon: '🍗', color: 'bg-red-100', textColor: 'text-red-800', borderColor: 'border-red-200' },
        'Drinks': { icon: '🥤', color: 'bg-blue-100', textColor: 'text-blue-800', borderColor: 'border-blue-200' }
      };
      return config[category] || { icon: '🍽️', color: 'bg-black-100', textColor: 'text-black-800', borderColor: 'border-gray-200' };
    };

    // ✅ API Call
    const searchDishes = async () => {
      setSearchError('');
      setDishes([]);

      if (!budget && !category && !timing) {
        setSearchError('Please enter budget or select category/timing');
        return;
      }

      setSearchLoading(true);

      try {
        // ✅ URL building that matches backend
        let url = '';

        if (budget && category && timing) {
          url = `${API_BASE_URL}/api/dishes/search/budget-timing?maxPrice=${budget}&timing=${timing}&category=${category}`;
        } else if (budget && category) {
          url = `${API_BASE_URL}/api/dishes/search/budget-category?maxPrice=${budget}&category=${category}`;
        } else if (budget && timing) {
          url = `${API_BASE_URL}/api/dishes/search/budget-timing?maxPrice=${budget}&timing=${timing}`;
        } else if (category && timing) {
          url = `${API_BASE_URL}/api/dishes/search/category-timing?category=${category}&timing=${timing}`;
        } else if (budget) {
          url = `${API_BASE_URL}/api/dishes/budget/${budget}`;
        } else if (category) {
          url = `${API_BASE_URL}/api/dishes/category/${category}`;
        } else if (timing) {
          url = `${API_BASE_URL}/api/dishes/timing/${timing}`;
        }

        const response = await fetch(url);

        if (!response.ok) {
          throw new Error(`Server returned ${response.status}`);
        }

        const data = await response.json();
        setDishes(data);

      } catch (err) {
        setSearchError('Failed to load dishes. Please try again.');
      } finally {
        setSearchLoading(false);
      }
    };

    const groupedDishes = groupDishesByCategory(dishes);
    const categories = Object.keys(groupedDishes);

    if (loading) {
      return (
        <div className="min-h-screen flex items-center justify-center">
          <div className="flex flex-col items-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mb-4"></div>
            <p className="text-gray-600">Loading your dashboard...</p>
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
          {/* Welcome Section */}
          <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              Welcome back, {user.name}! 👋
            </h1>
            <p className="text-gray-600">
              Find delicious food within your budget
            </p>
          </div>

          {/* Search Section */}
          <div className="bg-white p-6 rounded-lg shadow mb-6">
            <h2 className="text-xl font-semibold mb-4">🔍 Search Food</h2>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
              <input
                type="number"
                placeholder="Enter your budget (₹)"
                value={budget}
                onChange={(e) => setBudget(e.target.value)}
                className="border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                min="1"
              />

              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Categories</option>
                <option value="Veg">Veg</option>
                <option value="Non-Veg">Non-Veg</option>
                <option value="Drinks">Drinks</option>
              </select>

              <select
                value={timing}
                onChange={(e) => setTiming(e.target.value)}
                className="border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Any Time</option>
                <option value="Morning">Morning</option>
                <option value="Lunch">Lunch</option>
                <option value="Dinner">Dinner</option>
                <option value="Evening">Evening</option>
              </select>

              <button
                onClick={searchDishes}
                disabled={searchLoading}
                className="bg-blue-500 text-white px-6 py-3 rounded hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors font-medium flex items-center justify-center"
              >
                {searchLoading ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                    Searching...
                  </>
                ) : (
                  'Search'
                )}
              </button>
            </div>

            {searchError && (
              <div className="mb-4 p-3 bg-red-100 border border-red-300 text-red-700 rounded flex items-center">
                <span className="mr-2">⚠️</span>
                {searchError}
              </div>
            )}
          </div>

          {/* Results Section */}
          {dishes.length > 0 ? (
            <div className="bg-white p-6 rounded-lg shadow">
              <h3 className="text-xl font-semibold mb-6">
                🍽️ Found {dishes.length} dishes
                {budget && ` under ₹${budget}`}
              </h3>

              <div className="space-y-8">
                {categories.map(category => {
                  const categoryInfo = getCategoryInfo(category);
                  const categoryDishes = groupedDishes[category];

                  return (
                    <div key={category} className={`border-2 ${categoryInfo.borderColor} rounded-lg overflow-hidden`}>
                      <div className={`${categoryInfo.color} ${categoryInfo.textColor} p-4`}>
                        <h4 className="text-lg font-semibold flex items-center gap-2">
                          {categoryInfo.icon} {category}
                          <span className="text-sm font-normal bg-white px-2 py-1 rounded">
                            {categoryDishes.length} items
                          </span>
                        </h4>
                      </div>

                      <div className="p-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                          {categoryDishes.map((dish) => (
                            <div key={dish.dishId} className="border border-gray-200 p-4 rounded-lg bg-white shadow-sm hover:shadow-md transition-shadow">
                              <div className="mb-3">
                                <h4 className="font-semibold text-lg text-gray-800 mb-1">{dish.name}</h4>
                                <div className="flex justify-between text-sm text-gray-600">
                                  <span className={`px-2 py-1 rounded text-xs ${categoryInfo.color} ${categoryInfo.textColor}`}>
                                    {category}
                                  </span>
                                  <span className="text-green-600 font-bold text-lg">₹{dish.price}</span>
                                </div>
                                <p className="text-sm text-gray-500 mt-1">
                                  Status: <span className={dish.availability ? 'text-green-600 font-medium' : 'text-red-600 font-medium'}>
                                    {dish.availability ? '✅ Available' : '❌ Not Available'}
                                  </span>
                                </p>
                              </div>

                              <FeedbackButton dish={dish} />

                              {dish.restaurant ? (
                                <div className="mt-3 p-3 bg-blue-50 rounded-lg border border-blue-200">
                                  <p className="font-medium text-blue-800 text-sm mb-1 flex items-center">
                                    🏪 {dish.restaurant.name}
                                  </p>
                                  <p className="text-xs text-blue-700 mb-1 flex items-center">
                                    📍 {dish.restaurant.address || 'Address not available'}
                                  </p>
                                  <p className="text-xs text-gray-600 flex items-center">
                                    ⏰ {dish.restaurant.openingTime || '09:00'} - {dish.restaurant.closingTime || '22:00'}
                                  </p>
                                </div>
                              ) : (
                                <div className="mt-3 p-2 bg-yellow-50 rounded border border-yellow-200">
                                  <p className="text-xs text-yellow-700">🏪 Restaurant information not available</p>
                                </div>
                              )}

                              {(dish.quantity || dish.notes) && (
                                <div className="mt-2 pt-2 border-t border-gray-200">
                                  {dish.quantity && (
                                    <p className="text-xs text-gray-600">📏 {dish.quantity}</p>
                                  )}
                                  {dish.notes && (
                                    <p className="text-xs text-gray-500 mt-1">📝 {dish.notes}</p>
                                  )}
                                </div>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : (
            <div className="bg-white p-8 rounded-lg shadow text-center text-gray-500">
              <div className="text-6xl mb-4">🍕</div>
              <p className="text-lg mb-2">Ready to find some delicious food?</p>
              <p className="text-sm">Enter your budget and search for amazing dishes!</p>
            </div>
          )}
        </div>
      </div>
    );
  }