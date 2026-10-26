import { useEffect, useState } from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid
} from 'recharts';

interface Ride {
  id: string;
  name: string;
  waitTime: number | null;
  status: string;
  lastupdate: string;
}

interface HistoryPoint {
  waitTime: number | null;
  status: string;
  observedAt: string;
  formattedTime?: string;
}

interface HorizonPrediction {
  minutesAhead: number;
  predictedWait: number | null;
  expectedChange: number | null;
}

interface Prediction {
  rideId: string;
  name: string;
  currentWait: number | null;
  predictedWait: number | null;
  predictionMinutesAhead: number;
  expectedChange: number | null;
  recommendation: string;
  model: string;
  message: string;
  horizons?: HorizonPrediction[];
}

export default function App() {
  const [rides, setRides] = useState<Ride[]>([]);
  const [selectedRideId, setSelectedRideId] = useState<string>('');
  const [selectedRide, setSelectedRide] = useState<Ride | null>(null);
  const [history, setHistory] = useState<HistoryPoint[]>([]);
  const [prediction, setPrediction] = useState<Prediction | null>(null);
  const [historyLoading, setHistoryLoading] = useState<boolean>(false);
  const [predictionLoading, setPredictionLoading] = useState<boolean>(false);
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
        const sorted = data.sort((a, b) => a.name.localeCompare(b.name));
        setRides(sorted);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load rides');
        setLoading(false);
      });
  }, []);

  // 2. Fetch specific ride details + historical observations + predictions
  const handleRideChange = (id: string) => {
    setSelectedRideId(id);
    if (!id) {
      setSelectedRide(null);
      setHistory([]);
      setPrediction(null);
      return;
    }

    // A. Fetch current snapshot
    fetch(`/api/rides/${id}`)
      .then((res) => {
        if (!res.ok) throw new Error('Failed to fetch ride details');
        return res.json();
      })
      .then((data: Ride) => {
        setSelectedRide(data);
      })
      .catch((err) => console.error(err));

    // B. Fetch historical data from PostgreSQL
    setHistoryLoading(true);
    fetch(`/api/rides/${id}/history`)
      .then((res) => {
        if (!res.ok) throw new Error('Failed to fetch history');
        return res.json();
      })
      .then((data: HistoryPoint[]) => {
        const formatted = data.map((pt) => ({
          ...pt,
          formattedTime: new Date(pt.observedAt).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
          })
        }));
        setHistory(formatted);
        setHistoryLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setHistoryLoading(false);
      });

    // C. Fetch predictions (15, 30, 45 min)
    setPredictionLoading(true);
    fetch(`/api/rides/${id}/prediction`)
      .then((res) => {
        if (!res.ok) throw new Error('Failed to fetch prediction');
        return res.json();
      })
      .then((data: Prediction) => {
        setPrediction(data);
        setPredictionLoading(false);
      })
      .catch((err) => {
        console.error(err);
        setPredictionLoading(false);
      });
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center p-6">
      <div className="max-w-2xl w-full bg-slate-800/90 rounded-2xl shadow-xl border border-slate-700/60 p-8 space-y-6">
        
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
            Real-Time Theme Park Analytics & Multi-Horizon Wait Predictions
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
            {selectedRide && (
              <div className="mt-6 p-5 rounded-xl bg-slate-900/80 border border-slate-700/80 space-y-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h2 className="text-lg font-bold text-white">
                      {selectedRide.name}
                    </h2>
                    <p className="text-xs text-slate-400 mt-0.5">
                      Last Updated: {new Date(selectedRide.lastupdate).toLocaleTimeString()}
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

                {/* Multi-Horizon Wait Time Metrics: Current, +15m, +30m, +45m */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-3 border-t border-slate-800">
                  {/* Current Wait */}
                  <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-700/50 flex flex-col justify-between">
                    <span className="text-[11px] text-slate-400 font-medium">Current Wait</span>
                    <div className="mt-2">
                      {selectedRide.waitTime !== null ? (
                        <span className="text-2xl font-extrabold text-white">
                          {selectedRide.waitTime} <span className="text-xs font-medium text-slate-400">min</span>
                        </span>
                      ) : (
                        <span className="text-xs text-slate-400">N/A</span>
                      )}
                    </div>
                  </div>

                  {/* Multi-Horizons (15, 30, 45) */}
                  {([15, 30, 45] as const).map((minutes) => {
                    const horizon = prediction?.horizons?.find((h) => h.minutesAhead === minutes);
                    return (
                      <div
                        key={minutes}
                        className={`p-3 rounded-xl border flex flex-col justify-between ${
                          minutes === 30
                            ? 'bg-indigo-950/40 border-indigo-500/40 ring-1 ring-indigo-500/20'
                            : 'bg-slate-800/40 border-slate-700/50'
                        }`}
                      >
                        <div className="flex items-center justify-between gap-1">
                          <span className={`text-[11px] font-semibold ${minutes === 30 ? 'text-indigo-300' : 'text-slate-400'}`}>
                            +{minutes} min
                          </span>
                          {horizon && horizon.expectedChange !== null && horizon.expectedChange !== undefined && (
                            <span
                              className={`text-[10px] font-bold px-1.5 py-0.5 rounded leading-none ${
                                horizon.expectedChange > 0
                                  ? 'bg-rose-500/20 text-rose-300'
                                  : horizon.expectedChange < 0
                                  ? 'bg-emerald-500/20 text-emerald-300'
                                  : 'bg-slate-700 text-slate-300'
                              }`}
                            >
                              {horizon.expectedChange > 0 ? `+${horizon.expectedChange}m` : `${horizon.expectedChange}m`}
                            </span>
                          )}
                        </div>

                        <div className="mt-2">
                          {predictionLoading ? (
                            <span className="text-xs text-slate-400">...</span>
                          ) : horizon && horizon.predictedWait !== null && horizon.predictedWait !== undefined ? (
                            <span className={`text-2xl font-extrabold ${minutes === 30 ? 'text-indigo-400' : 'text-slate-200'}`}>
                              {horizon.predictedWait} <span className="text-xs font-medium text-slate-400">min</span>
                            </span>
                          ) : (
                            <span className="text-xs text-slate-400">N/A</span>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>

                {/* Recommendation Banner */}
                {prediction && (
                  <div className={`p-3 rounded-xl border text-xs flex items-center justify-between gap-3 ${
                    prediction.recommendation === 'RIDE_NOW'
                      ? 'bg-emerald-950/40 border-emerald-500/30 text-emerald-200'
                      : prediction.recommendation === 'CONSIDER_WAITING'
                      ? 'bg-amber-950/40 border-amber-500/30 text-amber-200'
                      : 'bg-slate-800/40 border-slate-700/60 text-slate-300'
                  }`}>
                    <div>
                      <span className="font-bold uppercase tracking-wider block text-[10px]">Recommendation</span>
                      <span>
                        {prediction.recommendation === 'RIDE_NOW' && '🚀 Ride now! Wait times are forecasted to surge.'}
                        {prediction.recommendation === 'CONSIDER_WAITING' && '⏳ Consider waiting. Wait times are trending downwards.'}
                        {prediction.recommendation === 'STABLE' && '✅ Wait times are holding steady. Good time to join line.'}
                        {prediction.recommendation === 'NO_DATA' && 'Attraction is closed or has insufficient data.'}
                      </span>
                    </div>
                    <span className="text-[10px] bg-slate-900/60 px-2 py-1 rounded border border-slate-700/50 font-mono text-slate-400 whitespace-nowrap">
                      {prediction.model}
                    </span>
                  </div>
                )}

                {/* Historical Wait Time Chart (from PostgreSQL) */}
                <div className="pt-3 border-t border-slate-800 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="text-xs font-semibold uppercase tracking-wider text-slate-300">
                        Wait Time History
                      </span>
                      <span className="ml-2 text-[11px] text-indigo-400">
                        ({history.length} snapshot{history.length === 1 ? '' : 's'})
                      </span>
                    </div>
                    <span className="text-[11px] text-slate-500">
                      Source: PostgreSQL
                    </span>
                  </div>

                  {historyLoading ? (
                    <div className="h-44 flex items-center justify-center text-xs text-slate-400">
                      Loading historical data...
                    </div>
                  ) : history.length > 0 ? (
                    <div className="h-48 w-full pt-2">
                      <ResponsiveContainer width="100%" height="100%">
                        <AreaChart data={history} margin={{ top: 10, right: 10, left: -20, bottom: 5 }}>
                          <defs>
                            <linearGradient id="waitGradient" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="5%" stopColor="#6366f1" stopOpacity={0.4}/>
                              <stop offset="95%" stopColor="#6366f1" stopOpacity={0.0}/>
                            </linearGradient>
                          </defs>
                          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                          <XAxis
                            dataKey="formattedTime"
                            stroke="#94a3b8"
                            fontSize={11}
                            tickLine={false}
                          />
                          <YAxis
                            domain={[0, 'dataMax + 15']}
                            stroke="#94a3b8"
                            fontSize={11}
                            tickLine={false}
                            unit="m"
                          />
                          <Tooltip
                            contentStyle={{
                              backgroundColor: '#0f172a',
                              borderColor: '#334155',
                              borderRadius: '0.75rem',
                              fontSize: '12px'
                            }}
                            labelStyle={{ color: '#94a3b8' }}
                            formatter={(value: any) => [`${value} min`, 'Wait Time']}
                          />
                          <Area
                            type="monotone"
                            dataKey="waitTime"
                            stroke="#6366f1"
                            strokeWidth={3}
                            fillOpacity={1}
                            fill="url(#waitGradient)"
                            dot={{ fill: '#818cf8', r: 5 }}
                            activeDot={{ r: 7 }}
                          />
                        </AreaChart>
                      </ResponsiveContainer>
                    </div>
                  ) : (
                    <div className="h-32 flex flex-col items-center justify-center text-xs text-slate-500 bg-slate-900/50 rounded-lg border border-slate-800/80 p-4 text-center">
                      <span>No historical observations stored yet for this ride.</span>
                      <span className="mt-1 text-[11px] text-slate-600">The scheduler records snapshots every minute.</span>
                    </div>
                  )}
                </div>
              </div>
            )}

            {!selectedRide && (
              <p className="text-xs text-center text-slate-500 py-3">
                Select an attraction above to view its live wait time, 15/30/45-min forecast, and historical trend.
              </p>
            )}
          </div>
        )}

      </div>
    </div>
  );
}
