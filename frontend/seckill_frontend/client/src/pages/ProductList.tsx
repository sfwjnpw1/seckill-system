import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useEffect, useState } from "react";
import { useLocation } from "wouter";
import { toast } from "sonner";
import { Clock, ShoppingCart } from "lucide-react";

interface SeckillActivity {
  id: number;
  productId: number;
  productName: string;
  seckillPrice: number;
  seckillStock: number;
  startTime: string;
  endTime: string;
  status: number;
}

export default function ProductList() {
  const [, setLocation] = useLocation();
  const [activities, setActivities] = useState<SeckillActivity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchActivities();
  }, []);

  const fetchActivities = async () => {
    try {
      const response = await fetch("http://localhost:8080/product/seckill/list");
      const result = await response.json();

      if (result.code === 200) {
        setActivities(result.data || []);
      } else {
        toast.error("Failed to load seckill activities");
      }
    } catch (error) {
      toast.error("Network error");
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: number, startTime: string, endTime: string) => {
    const now = new Date();
    const start = new Date(startTime);
    const end = new Date(endTime);

    if (now < start) {
      return <Badge variant="secondary">Not Started</Badge>;
    } else if (now > end) {
      return <Badge variant="destructive">Ended</Badge>;
    } else {
      return <Badge variant="default" className="bg-green-500">Active</Badge>;
    }
  };

  const formatTime = (time: string) => {
    return new Date(time).toLocaleString();
  };

  const handleViewDetail = (id: number) => {
    setLocation(`/seckill/${id}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Seckill System</h1>
          <div className="flex gap-4">
            <Button variant="outline" onClick={() => setLocation("/")}>
              Home
            </Button>
            <Button variant="outline" onClick={() => {
              localStorage.removeItem("token");
              localStorage.removeItem("user");
              setLocation("/login");
            }}>
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <div className="mb-6">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">Active Seckill Activities</h2>
          <p className="text-gray-600">Grab the best deals before they're gone!</p>
        </div>

        {activities.length === 0 ? (
          <Card className="text-center py-12">
            <CardContent>
              <ShoppingCart className="mx-auto h-12 w-12 text-gray-400 mb-4" />
              <p className="text-gray-600">No active seckill activities at the moment</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {activities.map((activity) => (
              <Card key={activity.id} className="hover:shadow-lg transition-shadow">
                <CardHeader>
                  <div className="flex justify-between items-start mb-2">
                    <CardTitle className="text-xl">{activity.productName}</CardTitle>
                    {getStatusBadge(activity.status, activity.startTime, activity.endTime)}
                  </div>
                  <CardDescription>
                    <div className="flex items-center gap-2 text-sm">
                      <Clock className="h-4 w-4" />
                      <span>{formatTime(activity.startTime)} - {formatTime(activity.endTime)}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">Seckill Price:</span>
                      <span className="text-2xl font-bold text-red-600">¥{activity.seckillPrice}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">Stock:</span>
                      <span className="text-sm font-semibold">{activity.seckillStock} items</span>
                    </div>
                  </div>
                </CardContent>
                <CardFooter>
                  <Button 
                    className="w-full" 
                    onClick={() => handleViewDetail(activity.id)}
                    disabled={new Date() > new Date(activity.endTime)}
                  >
                    {new Date() > new Date(activity.endTime) ? "Ended" : "View Details"}
                  </Button>
                </CardFooter>
              </Card>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
