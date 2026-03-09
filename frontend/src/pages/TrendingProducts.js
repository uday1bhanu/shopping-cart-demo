import React, { useState, useEffect } from 'react';
import { productsAPI } from '../services/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import ProductCard from '../components/ProductCard';
import '../styles/TrendingProducts.css';

const TrendingProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [loadTime, setLoadTime] = useState(0);
  const [showErrorModal, setShowErrorModal] = useState(false);
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    fetchTrendingProducts();
  }, []);

  const fetchTrendingProducts = async () => {
    const startTime = Date.now();
    setLoading(true);
    setError(null);

    // Update load time every 100ms while loading
    const interval = setInterval(() => {
      setLoadTime(((Date.now() - startTime) / 1000).toFixed(1));
    }, 100);

    try {
      const response = await productsAPI.getTrending();
      const endTime = Date.now();
      const duration = ((endTime - startTime) / 1000).toFixed(1);

      clearInterval(interval);
      setLoadTime(duration);
      setProducts(response.data);
      setLoading(false);

      // Show warning if it took too long
      if (duration > 3) {
        setError('We\'re having trouble loading recommendations right now. Please try again in a moment.');
        setShowErrorModal(true);
      }
    } catch (err) {
      clearInterval(interval);
      setLoading(false);

      setError('We\'re having trouble loading recommendations right now. Please try again in a moment.');
      setShowErrorModal(true);

      console.error('Error fetching trending products:', err);
      console.error('Error details:', {
        message: err.message,
        code: err.code,
        response: err.response,
      });
    }
  };

  const handleAddToCart = async (productId) => {
    if (!isAuthenticated) {
      alert('Please login to add items to cart');
      return;
    }

    try {
      await addToCart(productId, 1);
      alert('Product added to cart!');
    } catch (err) {
      alert('Failed to add product to cart');
    }
  };

  const closeErrorModal = () => {
    setShowErrorModal(false);
  };

  return (
    <div className="trending-products-page">
      <div className="trending-header">
        <h1>🔥 Trending Products</h1>
        <p className="trending-subtitle">
          Most popular products based on customer orders
        </p>
      </div>

      {loading && (
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p className="loading-text">Loading trending products...</p>
          <p className="load-time">Time elapsed: {loadTime}s</p>
          {parseFloat(loadTime) > 3 && (
            <p className="slow-warning">
              ⚠️ This is taking longer than expected. There might be a performance issue.
            </p>
          )}
          {parseFloat(loadTime) > 10 && (
            <p className="slow-warning critical">
              🔥 Critical: Server performance degradation detected!
            </p>
          )}
        </div>
      )}

      {!loading && !error && products.length === 0 && (
        <div className="no-products">
          <p>No trending products found.</p>
        </div>
      )}

      {!loading && products.length > 0 && (
        <>
          <div className="performance-info">
            <span className={`load-time-badge ${parseFloat(loadTime) > 5 ? 'slow' : parseFloat(loadTime) > 3 ? 'warning' : 'good'}`}>
              Loaded in {loadTime}s
            </span>
            {parseFloat(loadTime) > 3 && (
              <span className="performance-warning">
                ⚠️ Performance issue detected - Check server logs
              </span>
            )}
          </div>

          <div className="products-grid">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={handleAddToCart}
              />
            ))}
          </div>
        </>
      )}

      {showErrorModal && (
        <div className="error-modal-overlay" onClick={closeErrorModal}>
          <div className="error-modal" onClick={(e) => e.stopPropagation()}>
            <div className="error-modal-header">
              <h2>⚠️ Error</h2>
              <button className="close-button" onClick={closeErrorModal}>×</button>
            </div>
            <div className="error-modal-body">
              <p className="error-message">{error}</p>
              <div className="error-actions">
                <button className="btn btn-primary" onClick={fetchTrendingProducts}>
                  Try Again
                </button>
                <button className="btn btn-outline" onClick={closeErrorModal}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TrendingProducts;
