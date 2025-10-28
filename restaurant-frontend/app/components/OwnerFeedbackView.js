"use client";
import { useState, useEffect } from 'react';
import { useAuth } from './AuthContext';

export default function OwnerFeedbackView() {
  const { user, apiCall } = useAuth();
  const [restaurants, setRestaurants] = useState([]);
  const [selectedRestaurant, setSelectedRestaurant] = useState(null);
  const [feedbackData, setFeedbackData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadOwnerRestaurants();
  }, []);

  const loadOwnerRestaurants = async () => {
    try {
      const data = await apiCall('/owner/restaurants');
      setRestaurants(data);
      if (data.length > 0) {
        setSelectedRestaurant(data[0]);
      }
    } catch (error) {
      console.error('Error loading restaurants:', error);
    }
  };

  const loadRestaurantFeedback = async (restaurantId) => {
    setLoading(true);
    try {
      const data = await apiCall(`/feedback/owner/restaurants/${restaurantId}/feedback`);
      setFeedbackData(data);
    } catch (error) {
      console.error('Error loading feedback:', error);
      setFeedbackData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedRestaurant) {
      loadRestaurantFeedback(selectedRestaurant.restaurantId);
    }
  }, [selectedRestaurant]);

  if (!user || user.role !== 'OWNER') {
    return <div>Access denied. Owner login required.</div>;
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-gray-800">⭐ Customer Feedback</h2>

      {/* Restaurant Selection */}
      <div className="bg-white p-4 rounded-lg shadow">
        <h3 className="font-semibold mb-3">Select Restaurant:</h3>
        <div className="flex gap-2 overflow-x-auto pb-2">
          {restaurants.map(restaurant => (
            <button
              key={restaurant.restaurantId}
              onClick={() => setSelectedRestaurant(restaurant)}
              className={`px-4 py-2 rounded whitespace-nowrap transition-colors ${
                selectedRestaurant?.restaurantId === restaurant.restaurantId
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              {restaurant.name}
            </button>
          ))}
        </div>
      </div>

      {/* Feedback Summary */}
      {feedbackData && (
        <div className="bg-blue-50 p-6 rounded-lg border border-blue-200">
          <h3 className="text-xl font-semibold text-blue-800 mb-4">
            {feedbackData.restaurantName} - Feedback Summary
          </h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="text-3xl font-bold text-blue-600">{feedbackData.averageRating}/5</div>
              <div className="text-sm text-blue-700">Average Rating</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-green-600">{feedbackData.totalFeedback}</div>
              <div className="text-sm text-green-700">Total Reviews</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-purple-600">
                {feedbackData.feedback?.filter(fb => fb.rating >= 4).length || 0}
              </div>
              <div className="text-sm text-purple-700">Positive Reviews</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-orange-600">
                {feedbackData.feedback?.filter(fb => fb.rating <= 2).length || 0}
              </div>
              <div className="text-sm text-orange-700">Needs Improvement</div>
            </div>
          </div>
        </div>
      )}

      {/* Feedback List */}
      <div className="bg-white p-6 rounded-lg shadow">
        <h3 className="text-xl font-semibold mb-4">Customer Reviews</h3>

        {loading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
            <p className="mt-2 text-gray-600">Loading feedback...</p>
          </div>
        ) : feedbackData?.feedback?.length > 0 ? (
          <div className="space-y-4">
            {feedbackData.feedback.map(fb => (
              <div key={fb.feedbackId} className="border border-gray-200 p-4 rounded-lg hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start mb-2">
                  <div>
                    <h4 className="font-semibold text-lg text-gray-800">{fb.dishName}</h4>
                    <div className="flex items-center gap-2 mt-1">
                      <span className="text-yellow-500 text-lg">
                        {'★'.repeat(fb.rating)}{'☆'.repeat(5-fb.rating)}
                      </span>
                      <span className="text-sm text-gray-500">({fb.rating}/5)</span>
                    </div>
                  </div>
                  <span className="text-sm text-gray-400">
                    {new Date(fb.createdAt).toLocaleDateString()}
                  </span>
                </div>

                {fb.comment && (
                  <div className="mt-3 p-3 bg-gray-50 rounded border">
                    <p className="text-gray-700 italic">"{fb.comment}"</p>
                  </div>
                )}

                <div className="mt-2 text-sm text-gray-500">
                  Reviewed by: <span className="font-medium">{fb.studentName}</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8 text-gray-500">
            <div className="text-6xl mb-4">💬</div>
            <p className="text-lg">No feedback yet for this restaurant</p>
            <p className="text-sm mt-2">Customer reviews will appear here</p>
          </div>
        )}
      </div>
    </div>
  );
}