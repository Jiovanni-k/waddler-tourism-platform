// Booking statuses from backend BookingStatus enum:
// PENDING | CONFIRMED | CANCELLED | CHECKED_IN | CHECKED_OUT | NO_SHOW

const STATUS_STYLES = {
  PENDING:     { label: "Pending",     className: "badge badge-pending"     },
  CONFIRMED:   { label: "Confirmed",   className: "badge badge-confirmed"   },
  CANCELLED:   { label: "Cancelled",   className: "badge badge-cancelled"   },
  CHECKED_IN:  { label: "Checked In",  className: "badge badge-checked-in"  },
  CHECKED_OUT: { label: "Checked Out", className: "badge badge-checked-out" },
  NO_SHOW:     { label: "No Show",     className: "badge badge-no-show"     },
};

const BookingStatusBadge = ({ status }) => {
  const config = STATUS_STYLES[status] || { label: status, className: "badge badge-default" };
  return <span className={config.className}>{config.label}</span>;
};

export default BookingStatusBadge;
