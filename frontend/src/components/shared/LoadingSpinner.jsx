const LoadingSpinner = ({ message = "Loading..." }) => {
  return (
    <div className="spinner-wrapper">
      <div className="spinner" />
      {message && <p className="spinner-message">{message}</p>}
    </div>
  );
};

export default LoadingSpinner;
