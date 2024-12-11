import { useState } from "react";
import "./App.css";
import { API } from '@stoplight/elements';
import '@stoplight/elements/styles.min.css';
import raw from "raw.macro";

const apiyaml = raw('./openapi.yaml')

export default function App() {
  const [count, setCount] = useState(null);

  return (
      <div className="App">
        <API
            apiDescriptionDocument={apiyaml}
        />
      </div>
  );
}
