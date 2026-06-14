import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const UnauthorizedPage = () => {
  const { user } = useAuth();
  const navigate  = useNavigate();

  return (
    <div className="unauthorized-page">
      <div className="unauthorized-box">
        <div className="unauthorized-icon">🚫</div>
        <h1 className="unauthorized-title">Access Denied</h1>
        <p className="unauthorized-message">
          You don't have permission to view this page.
        </p>

        <div className="unauthorized-actions">
          <button className="btn btn-primary" onClick={() => navigate(-1)}>
            ← Go Back
          </button>
          <button className="btn btn-outline" onClick={() => navigate("/")}>
            Home
          </button>
          {!user && (
            <button className="btn btn-outline" onClick={() => navigate("/login")}>
              Sign In
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;
