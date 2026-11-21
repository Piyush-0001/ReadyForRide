
const API = "http://localhost:8080";

function setStatus(message) {
    document.getElementById("paymentStatus").innerText =
        message;
}

async function startRazorpayPayment() {

    const token = sessionStorage.getItem("jwt");
    const bookingId = sessionStorage.getItem("bookingId");
    const payBtn = document.getElementById("payBtn");

    if (!token || !bookingId) {
        alert("Please log in and create a booking first.");
        window.location.href = "login.html";
        return;
    }

    payBtn.disabled = true;
    setStatus("Creating Razorpay order...");

    try {
        const response = await fetch(
            `${API}/api/payments/${bookingId}/order`,
            {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }
        );

        if (!response.ok) {
            throw new Error(
                "Unable to create order. The booking may have expired."
            );
        }

        const order = await response.json();

        if (typeof Razorpay === "undefined") {
            throw new Error("Razorpay Checkout failed to load");
        }

        const options = {
            key: order.keyId,
            amount: order.amount,
            currency: order.currency,
            name: "ReadyForRide",
            description: "Vehicle Rental Booking",
            order_id: order.razorpayOrderId,

            handler: async function (paymentResponse) {

                setStatus("Verifying payment...");
                payBtn.disabled = true;

                try {
                    const verify = await fetch(
                        `${API}/api/payments/${bookingId}/verify`,
                        {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json",
                                "Authorization": `Bearer ${token}`
                            },
                            body: JSON.stringify({
                                razorpayOrderId:
                                    paymentResponse.razorpay_order_id,
                                razorpayPaymentId:
                                    paymentResponse.razorpay_payment_id,
                                razorpaySignature:
                                    paymentResponse.razorpay_signature
                            })
                        }
                    );

                    if (!verify.ok) {
                        throw new Error(
                            "Payment verification needs review"
                        );
                    }

                    const booking = await verify.json();

                    if (booking.status !== "CONFIRMED") {
                        setStatus(
                            "Payment received, but booking is not confirmed. " +
                            "Status: " + booking.status +
                            ". Contact support; do not pay again."
                        );
                        return;
                    }

                    sessionStorage.setItem(
                        "confirmedBooking",
                        JSON.stringify({
                            ...booking,
                            paymentId:
                                paymentResponse.razorpay_payment_id
                        })
                    );

                    window.location.href =
                        "confirmation.html";

                } catch (error) {
                    console.error(error);
                    setStatus(
                        "Payment may have succeeded, but confirmation " +
                        "could not be verified. Do not pay again. " +
                        "Contact support with booking ID: " + bookingId
                    );
                }
            },

            modal: {
                ondismiss: function () {
                    setStatus("Payment window closed.");
                    payBtn.disabled = false;
                }
            },

            theme: {
                color: "#2563eb"
            }
        };

        const razorpay = new Razorpay(options);

        razorpay.on("payment.failed", function () {
            setStatus(
                "Payment failed. You may retry while the " +
                "booking hold is active."
            );
            payBtn.disabled = false;
        });

        setStatus("Complete the test payment...");
        razorpay.open();

    } catch (error) {
        console.error(error);
        setStatus(error.message);
        payBtn.disabled = false;
    }
}
