"use client";
import { useState } from 'react';
import { useAuth } from './AuthContext';

export default function FeedbackButton({ dish }) {
  const { user, apiCall } = useAuth();
  const [showModal, setShowModal] = useState(false);
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);

  const submitFeedback = async () => {
    if (rating === 0) {
      alert('Please select a rating');
      return;
    }

    setLoading(true);
    try {
      const response = await apiCall(`/feedback/student/dishes/${dish.dishId}/add`, {
        method: 'POST',
        body: { rating, comment }
      });

      alert('✅ Feedback submitted successfully!');
      setShowModal(false);
      setRating(0);
      setComment('');

      // Refresh the page to show updated ratings
      setTimeout(() => {
        window.location.reload();
      }, 1000);

    } catch (error) {
      alert('❌ Error: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  if (user?.role !== 'STUDENT') {
    return null; // Only students can give feedback
  }

  return (
    <>
      {/* Feedback Button - Simple Design */}
      <button
        onClick={() => setShowModal(true)}
        className="bg-purple-500 text-white px-3 py-1 rounded text-sm hover:bg-purple-600 mt-2 transition-colors"
      >
        💬 Give Feedback
      </button>

      {/* Feedback Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white p-6 rounded-lg w-full max-w-md">
            <h3 className="text-xl font-bold mb-4">Rate {dish.name}</h3>

            {/* Star Rating - Simple */}
            <div className="mb-4">
              <label className="block mb-2 font-medium">Your Rating:</label>
              <div className="flex gap-1">
                {[1, 2, 3, 4, 5].map(star => (
                  <button
                    key={star}
                    onClick={() => setRating(star)}
                    className={`text-2xl ${rating >= star ? 'text-yellow-400' : 'text-gray-300'} hover:scale-110 transition-transform`}
                  >
                    ★
                  </button>
                ))}
              </div>
              <p className="text-sm text-gray-600 mt-1">{rating > 0 ? `You rated: ${rating} star${rating > 1 ? 's' : ''}` : 'Select stars'}</p>
            </div>

            {/* Comment */}
            <div className="mb-4">
              <label className="block mb-2 font-medium">Comment (optional):</label>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                className="w-full border border-gray-300 p-3 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows="3"
                placeholder="Share your experience with this dish..."
              />
            </div>

            {/* Buttons */}
            <div className="flex gap-3">
              <button
                onClick={submitFeedback}
                disabled={loading}
                className="bg-green-500 text-white px-4 py-2 rounded flex-1 hover:bg-green-600 disabled:bg-gray-400 transition-colors font-medium"
              >
                {loading ? (
                  <span className="flex items-center justify-center">
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Submitting...
                  </span>
                ) : (
                  'Submit Feedback'
                )}
              </button>
              <button
                onClick={() => setShowModal(false)}
                className="bg-gray-500 text-white px-4 py-2 rounded flex-1 hover:bg-gray-600 transition-colors font-medium"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}