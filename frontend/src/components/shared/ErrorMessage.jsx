import { AlertTriangle } from "lucide-react";

const ErrorMessage = ({ message = "Something went wrong. Please try again." }) => {
  return (
    <div className="error-message">
      <span className="error-icon"><AlertTriangle size={16} /></span>
      <p>{message}</p>
    </div>
  );
};

export default ErrorMessage;
