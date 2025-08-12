import { useEffect, useState } from "react";
import api from "./api";

function App() {
  const [customers, setCustomers] = useState([]);

  useEffect(() => {
    api.get("/customers")
      .then(res => setCustomers(res.data))
      .catch(err => console.error(err));
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>RoyalMedico Customers</h1>
      <ul>
        {customers.map(c => (
          <li key={c.id}>
            {c.name} — {c.email}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
