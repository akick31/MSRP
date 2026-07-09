interface LoadingScreenProps {
  error: string | null;
}

export default function LoadingScreen({ error }: LoadingScreenProps) {
  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[70vh] text-msrp-text px-4">
        <h1 className="text-2xl font-bold mb-4">MSRP</h1>
        <p className="text-msrp-red text-center">{error}</p>
        <button
          onClick={() => window.location.reload()}
          className="mt-4 px-6 py-2 bg-msrp-accent text-msrp-bg font-semibold rounded-lg"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-[70vh] text-msrp-text">
      <h1 className="text-2xl font-bold mb-4">MSRP</h1>
      <div className="w-8 h-8 border-2 border-msrp-accent border-t-transparent rounded-full animate-spin" />
    </div>
  );
}
