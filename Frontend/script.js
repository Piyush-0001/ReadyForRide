let vehicles = [];

const owner = {
  phone: "9876543210",
  address: "Location-1/237 Vikalp Khand, Gomti Nagar, Lucknow - 226010",
};

function renderVehicles(list) {
  const container = document.getElementById("carsContainer");
  container.innerHTML = "";

  list.forEach((v, index) => {
    const card = `
      <div class="car-card" data-price="${v.price}">


        <img src="${v.image}" alt="${v.name}">

        <h3>${v.name}</h3>

        <p>Type: ${v.type}</p>
        <p>KM Limit: ${v.kmLimit} km / day</p>
        <p>Security Deposit: ₹${v.security}</p>

        <div class="price">₹ ${v.price} / day</div>

        <button onclick='openDetails(${JSON.stringify(v)})'>

          View Details
        </button>

      </div>
    `;

    container.innerHTML += card;
  });
}

async function loadVehicles() {
  const container = document.getElementById("carsContainer");

  if (!container) return;

  container.innerHTML = "<p>Loading vehicles...</p>";

  try {
    const response = await fetch("http://localhost:8080/api/vehicles");

    if (!response.ok) {
      throw new Error("Failed to load vehicles");
    }

    const data = await response.json();

    vehicles = data
      .filter((v) => v.active)
      .map((v) => ({
        id: v.id,
        inventoryCode: v.inventoryCode,
        name: `${v.make} ${v.model}`,
        type: v.category,
        transmission: v.transmission,
        seats: v.seats,
        description: v.description || "",
        price: Number(v.dailyRate),
        image: v.imageUrl || "images/logo.jpeg",
        fuel: "Not specified",
        kmLimit: 0,
        security: 0,
      }));

    renderVehicles(vehicles);
  } catch (error) {
    console.error("Error loading vehicles:", error);
    container.innerHTML =
      "<p>Could not load vehicles. Please check the backend connection.</p>";
  }
}

/* ================================
   GLOBAL
================================ */

let pricePerDay = 0;

/* ================================
   OPEN DETAILS PAGE
================================ */

function openDetails(vehicle) {
  localStorage.setItem("selectedVehicle", JSON.stringify(vehicle));
  window.location.href = "details.html";
}

/* ================================
   LOAD DATA ON PAGES
================================ */

document.addEventListener("DOMContentLoaded", function () {
  const carData = localStorage.getItem("selectedVehicle");

  if (carData && document.getElementById("carName")) {
    const car = JSON.parse(carData);

    document.getElementById("carName").innerText = car.name;
    document.getElementById("carImg").src = car.image;
    document.getElementById("price").innerText = car.price;

    document.getElementById("carType").innerText = car.type;
    document.getElementById("fuel").innerText = car.fuel;
    document.getElementById("seats").innerText = car.seats;

    document.getElementById("kmLimit").innerText = car.kmLimit;
    document.getElementById("security").innerText = car.security;

    document.getElementById("vehicleField").value =
      car.name + " (" + car.type + ")";

    pricePerDay = car.price;
    setupAutoCalculation();
  }

  if (document.getElementById("ownerPhone")) {
    document.getElementById("ownerPhone").innerText = owner.phone;
    document.getElementById("ownerAddress").innerText = owner.address;
  }

  // ✅ HOMEPAGE CONTACT
  if (document.getElementById("homePhone")) {
    document.getElementById("homePhone").innerText = owner.phone;
    document.getElementById("homeAddress").innerText = owner.address;
  }

  /* ---------- SUMMARY PAGE ---------- */
  const bookingData = localStorage.getItem("booking");

  if (bookingData && document.getElementById("sCar")) {
    const b = JSON.parse(bookingData);

    document.getElementById("sCar").innerText = b.car;
    document.getElementById("sFrom").innerText = b.from;
    document.getElementById("sTo").innerText = b.to;
    document.getElementById("sDays").innerText = b.days;
    document.getElementById("sPrice").innerText = b.price;
    document.getElementById("sTotal").innerText = b.total;
  }
});

/* ================================
   AUTO PRICE CALCULATION
================================ */

function setupAutoCalculation() {
  const fromDate = document.getElementById("fromDate");
  const toDate = document.getElementById("toDate");

  if (!fromDate || !toDate) return;

  // block past dates
  const today = new Date().toISOString().split("T")[0];
  fromDate.min = today;
  toDate.min = today;

  fromDate.addEventListener("change", autoCalculate);
  toDate.addEventListener("change", autoCalculate);
}

function autoCalculate() {
  const from = document.getElementById("fromDate").value;
  const to = document.getElementById("toDate").value;

  if (!from || !to) return;

  const days = Math.ceil(
    (new Date(to) - new Date(from)) / (1000 * 60 * 60 * 24),
  );

  if (days <= 0) return;

  const vehicle = JSON.parse(localStorage.getItem("selectedVehicle"));

  const rentTotal = days * vehicle.price;
  const finalTotal = rentTotal + vehicle.security;

  document.getElementById("calcDays").innerText =
    `${vehicle.price} x ${days} Days = ₹${rentTotal}`;

  document.getElementById("calcRent").innerText = `Rent Total = ₹${rentTotal}`;

  document.getElementById("calcSecurity").innerText =
    `Security Deposit = ₹${vehicle.security}`;

  document.getElementById("calcFinal").innerText =
    `Total Payable = ₹${finalTotal}`;
}



/* =========================
   VEHICLE FILTERING
========================= */

const searchInput = document.getElementById("searchInput");
const typeFilter = document.getElementById("typeFilter");
const sortSelect = document.getElementById("sortPrice");
const resetBtn = document.getElementById("resetBtn");

function filterCars() {

    const search = searchInput?.value
        .trim().toLowerCase() || "";

    const category = typeFilter?.value
        .trim().toUpperCase() || "";

    const sort = sortSelect?.value || "";

    let filtered = vehicles.filter(vehicle => {

        const nameMatch = vehicle.name
            .toLowerCase()
            .includes(search);

        const categoryMatch =
            category === "" ||
            vehicle.type.toUpperCase() === category;

        return nameMatch && categoryMatch;
    });

    if (sort === "low") {
        filtered.sort((a, b) => a.price - b.price);
    }

    if (sort === "high") {
        filtered.sort((a, b) => b.price - a.price);
    }

    renderVehicles(filtered);

    if (filtered.length === 0) {
        const container =
            document.getElementById("carsContainer");

        if (container) {
            container.innerHTML =
                "<p>No matching vehicles found.</p>";
        }
    }
}

// Search
searchInput?.addEventListener("input", filterCars);

// Car / Bike filter
typeFilter?.addEventListener("change", filterCars);

// Price sorting
sortSelect?.addEventListener("change", filterCars);

// Reset
resetBtn?.addEventListener("click", () => {

    if (searchInput) searchInput.value = "";
    if (typeFilter) typeFilter.value = "";
    if (sortSelect) sortSelect.value = "";

    filterCars();
});


function showPayment(type) {
  document.getElementById("card").style.display = "none";
  document.getElementById("upi").style.display = "none";
  document.getElementById("netbanking").style.display = "none";

  document.getElementById(type).style.display = "block";
}

document.addEventListener("DOMContentLoaded", function () {
  const data = localStorage.getItem("confirmation");

  if (data && document.getElementById("bookingId")) {
    const c = JSON.parse(data);

    document.getElementById("bookingId").innerText = c.bookingId;
    document.getElementById("cCar").innerText = c.car;
    document.getElementById("cFrom").innerText = c.from;
    document.getElementById("cTo").innerText = c.to;
    document.getElementById("cDays").innerText = c.days;
    document.getElementById("cTotal").innerText = c.total;
    document.getElementById("cMethod").innerText = c.method;
    document.getElementById("cTxn").innerText = c.txnId;
    document.getElementById("cPaid").innerText = c.paidAmount;
  }
});

function goHome() {
  localStorage.clear();
  window.location.href = "index.html";
}

function confirmManualPayment() {
  const txnId = document.getElementById("txnId").value;
  const amount = document.getElementById("paidAmount").value;

  if (txnId === "" || amount === "") {
    alert("Enter transaction details");
    return;
  }

  const paymentInfo = {
    method: "UPI",
    txnId: txnId,
    paidAmount: amount,
    time: new Date().toLocaleString(),
  };

  localStorage.setItem("payment", JSON.stringify(paymentInfo));

  payNow(); // existing function
}

function downloadReceipt() {
  const data = localStorage.getItem("confirmation");
  if (!data) {
    alert("Receipt data not found");
    return;
  }

  if (!window.jspdf) {
    alert("PDF library not loaded. Refresh page.");
    return;
  }

  const c = JSON.parse(data);
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF();

  // Title
  doc.setFontSize(18);
  doc.text("CAR RENTAL RECEIPT", 60, 20);

  // Invoice
  const invoiceNo = "INV-" + Date.now();
  doc.setFontSize(12);
  doc.text("Invoice No: " + invoiceNo, 20, 35);
  doc.text("Booking ID: " + c.bookingId, 20, 45);

  doc.text("Car: " + c.car, 20, 60);
  doc.text("From: " + c.from, 20, 70);
  doc.text("To: " + c.to, 20, 80);
  doc.text("Total Days: " + c.days, 20, 90);

  doc.text("Total Amount: ₹" + c.total, 20, 105);
  doc.text("Payment Method: " + c.method, 20, 115);
  doc.text("Transaction ID: " + c.txnId, 20, 125);
  doc.text("Paid Amount: ₹" + c.paidAmount, 20, 135);

  // Footer
  doc.line(20, 260, 190, 260);
  doc.setFontSize(10);
  doc.text("CarRent Pvt Ltd", 20, 270);
  doc.text("Email: support@carrent.com", 20, 276);
  doc.text("Phone: +91 9876543210", 20, 282);

  doc.save("receipt.pdf");

  localStorage.clear();
  setTimeout(() => {
    window.location.href = "index.html";
  }, 1000);
}

loadVehicles();

function contactOwner() {
  alert("Call Owner at " + owner.phone);
}

function sendToWhatsApp() {
  const name = document.getElementById("custName").value;
  const phone = document.getElementById("custPhone").value;
  const email = document.getElementById("custEmail").value;
  const location = document.getElementById("custLocation").value;

  const from = document.getElementById("fromDate").value;
  const to = document.getElementById("toDate").value;

  if (!name || !phone || !from || !to) {
    alert("Please fill required fields");
    return;
  }

  const vehicle = JSON.parse(localStorage.getItem("selectedVehicle"));
  const totalText =
    document.getElementById("calcFinal").innerText || "Not calculated";

  const message = `Hello, I want to book a vehicle.

Name: ${name}
Phone: ${phone}
Email: ${email}
Location: ${location}

Vehicle: ${vehicle.name}
Type: ${vehicle.type}

From: ${from}
To: ${to}

${totalText}`;

  const url = `https://wa.me/919415668780?text=${encodeURIComponent(message)}`;

  window.open(url, "_blank");
}


async function loginCustomer() {
    const email =
        document.getElementById("loginEmail").value;

    const password =
        document.getElementById("loginPassword").value;

    try {
        const response = await fetch(
            "http://localhost:8080/api/auth/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, password })
            }
        );

        if (!response.ok) {
            alert("Invalid email or password");
            return;
        }

        const data = await response.json();

        sessionStorage.setItem("jwt", data.token);
        sessionStorage.setItem("userId", data.userId);

        window.location.href = "index.html";

    } catch (error) {
        console.error(error);
        alert("Unable to connect to the backend");
    }
}

async function createCustomerBooking() {

    const token = sessionStorage.getItem("jwt");

    if (!token) {
        alert("Please log in before booking");
        window.location.href = "login.html";
        return;
    }

    const vehicle = JSON.parse(
        localStorage.getItem("selectedVehicle")
    );

    const startDate =
        document.getElementById("fromDate").value;

    const endDate =
        document.getElementById("toDate").value;

    if (!vehicle || !startDate || !endDate) {
        alert("Please select valid booking details");
        return;
    }

    try {
        const response = await fetch(
            "http://localhost:8080/api/bookings",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    vehicleId: vehicle.id,
                    startDate: startDate,
                    endDate: endDate
                })
            }
        );

        if (response.status === 401) {
            sessionStorage.removeItem("jwt");
            alert("Login expired. Please log in again.");
            window.location.href = "login.html";
            return;
        }

        if (response.status === 409) {
            alert("Vehicle already booked for these dates");
            return;
        }

        if (!response.ok) {
            alert("Booking failed. Check dates and availability.");
            return;
        }

        const booking = await response.json();

        sessionStorage.setItem(
            "bookingId", booking.bookingId
        );

        window.location.href = "payment.html";

    } catch (error) {
        console.error(error);
        alert("Unable to create booking");
    }
}
