import { history, theme } from "../store";
import History from "../components/History";

export default function HistoryPage() {
  return (
    <History
      history={history}
      theme={theme}
    />
  );
}
