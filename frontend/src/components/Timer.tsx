import React, { useState, useEffect, useCallback } from 'react';

interface TimerProps {
  remainingSeconds: number;
  onTimeUp: () => void;
}

const Timer: React.FC<TimerProps> = ({ remainingSeconds: initialSeconds, onTimeUp }) => {
  const [seconds, setSeconds] = useState(initialSeconds);

  useEffect(() => {
    setSeconds(initialSeconds);
  }, [initialSeconds]);

  useEffect(() => {
    if (seconds <= 0) {
      onTimeUp();
      return;
    }
    const timer = setInterval(() => {
      setSeconds((prev) => prev - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [seconds, onTimeUp]);

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  const isWarning = seconds <= 300;
  const isDanger = seconds <= 60;

  return (
    <div
      className={`px-4 py-2 rounded-lg font-mono text-lg font-bold ${
        isDanger
          ? 'bg-red-100 text-red-700 animate-pulse'
          : isWarning
          ? 'bg-yellow-100 text-yellow-700'
          : 'bg-blue-100 text-blue-700'
      }`}
    >
      {hours > 0 && `${hours}:`}
      {String(minutes).padStart(2, '0')}:{String(secs).padStart(2, '0')}
    </div>
  );
};

export default Timer;
