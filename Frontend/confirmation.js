
document.addEventListener("DOMContentLoaded", () => {

    const raw = sessionStorage.getItem("confirmedBooking");

    if (!raw) {
        window.location.href = "index.html";
        return;
    }

    const booking = JSON.parse(raw);

    if (booking.status !== "CONFIRMED") {
        window.location.href = "index.html";
        return;
    }

    const days = Math.round(
        (Date.parse(booking.endDate) -
         Date.parse(booking.startDate)) / 86400000
    );

    document.getElementById("bookingId").innerText =
        booking.bookingId;

    document.getElementById("cCar").innerText =
        booking.vehicleName;

    document.getElementById("cFrom").innerText =
        booking.startDate;

    document.getElementById("cTo").innerText =
        booking.endDate;

    document.getElementById("cDays").innerText = days;

    document.getElementById("cTotal").innerText =
        booking.totalAmount;

    document.getElementById("cMethod").innerText =
        "Razorpay Test Mode";

    document.getElementById("cTxn").innerText =
        booking.paymentId;

    document.getElementById("cPaid").innerText =
        booking.totalAmount;
});
