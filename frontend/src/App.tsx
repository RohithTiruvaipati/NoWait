import { useEffect, useState } from 'react';

interface Ride {
  id: string;
  name: string;
  waitTime: number | null;
  status: string;
  lastupdate: string;
}

export default function App() {
  const [rides, setRides] = useState<Ride[]>([]);
  const [selectedRideId, setSelectedRideId] = useState<string>('');
  const [selectedRide, setSelectedRide] = useState<Ride | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // 1. Fetch all rides on load to populate the dropdown
  useEffect(() => {
    fetch('/api/rides')
      .then((res) => {
        if (!res.ok) {
          throw new Error(`Server returned ${res.status}`);
        }
        return res.json();
      })
      .then((data: Ride[]) => {
        // Sort alphabetically by ride name
        const sorted = data.sort((a, b) => a.name.localeCompare(b.name));
        setRides(sorted);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load rides');
        setLoading(false);
      });
  }, []);

  // 2. Fetch specific ride details when user selects from dropdown
  const handleRideChange = (id: string) => {
    setSelectedRideId(id);
    if (!id) {
      setSelectedRide(null);
      return;
    }

    fetch(`/api/rides/${id}`)
      .then((res) => {
        if (!res.ok) {
          throw new Error('Failed to fetch ride details');
        }
        return res.json();
      })
      .then((data: Ride) => {
        setSelectedRide(data);
      })
      .catch((err) => {
        console.error(err);
      });
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center p-6">
      <div className="max-w-lg w-full bg-slate-800/90 rounded-2xl shadow-xl border border-slate-700/60 p-8 space-y-6">
        
        {/* Header */}
        <div className="text-center space-y-1">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-medium bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 mb-2">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            Live Disney Magic Kingdom
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight text-white">
            ParkPulse
          </h1>
          <p className="text-sm text-slate-400">
            Real-Time Theme Park Analytics & Wait Times
          </p>
        </div>

        {/* Loading / Error States */}
        {loading && (
          <div className="py-8 text-center text-slate-400 text-sm">
            Fetching attraction data from backend...
          </div>
        )}

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-xl text-sm text-center">
            {error}. Ensure your Spring Boot backend is running on port 8080.
          </div>
        )}

        {/* Dropdown Selector */}
        {!loading && !error && (
          <div className="space-y-4">
            <div>
              <label htmlFor="ride-select" className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-2">
                Select an Attraction
              </label>
              <select
                id="ride-select"
                value={selectedRideId}
                onChange={(e) => handleRideChange(e.target.value)}
                className="w-full bg-slate-900/90 border border-slate-600 rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all cursor-pointer"
              >
                <option value="">-- Choose a ride ({rides.length} available) --</option>
                {rides.map((ride) => (
                  <option key={ride.id} value={ride.id}>
                    {ride.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Selected Ride Detail Card */}
            {selectedRide ? (
              <div className="mt-6 p-5 rounded-xl bg-slate-900/80 border border-slate-700/80 space-y-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h2 className="text-lg font-bold text-white">
                      {selectedRide.name}
                    </h2>
                    <p className="text-xs text-slate-400 mt-0.5">
                      Updated: {new Date(selectedRide.lastupdate).toLocaleTimeString()}
                    </p>
                  </div>
                  <span
                    className={`px-2.5 py-1 rounded-md text-xs font-semibold uppercase tracking-wide ${
                      selectedRide.status === 'OPERATING'
                        ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                        : 'bg-rose-500/10 text-rose-400 border border-rose-500/30'
                    }`}
                  >
                    {selectedRide.status}
                  </span>
                </div>

                {/* Wait Time Metric */}
                <div className="pt-2 border-t border-slate-800 flex items-center justify-between">
                  <span className="text-sm text-slate-400">Standby Wait Time</span>
                  <div className="text-right">
                    {selectedRide.waitTime !== null ? (
                      <span className="text-3xl font-extrabold text-white">
                        {selectedRide.waitTime} <span className="text-sm font-medium text-slate-400">min</span>
                      </span>
                    ) : (
                      <span className="text-sm font-medium text-slate-400">
                        N/A (Closed or No Standby)
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <p className="text-xs text-center text-slate-400 py-3">
                Select an attraction above to view its live wait time.
              </p>
            )}
          </div>
        )}

      </div>
    </div>
  );
}
