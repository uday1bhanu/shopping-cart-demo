import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import '../styles/Navbar.css';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/products" className="navbar-brand">
          Shopping Cart
        </Link>
        <div className="navbar-menu">
          <Link to="/products" className="navbar-link">
            Products
          </Link>
          <Link to="/trending" className="navbar-link">
            🔥 Trending
          </Link>
          {isAuthenticated ? (
            <>
              <Link to="/cart" className="navbar-link cart-link">
                Cart
                {cart && cart.totalItems > 0 && (
                  <span className="cart-badge">{cart.totalItems}</span>
                )}
              </Link>
              <Link to="/orders" className="navbar-link">
                Orders
              </Link>
              <span className="navbar-user">
                Hello, {user?.username || user?.firstName}
              </span>
              <button onClick={handleLogout} className="btn btn-sm btn-outline">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-sm btn-primary">
                Login
              </Link>
              <Link to="/register" className="btn btn-sm btn-outline">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
