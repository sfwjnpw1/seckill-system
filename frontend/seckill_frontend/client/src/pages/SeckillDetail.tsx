import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useEffect, useState } from "react";
import { useLocation, useParams } from "wouter";
import { toast } from "sonner";
import { Clock, Package, Tag, ArrowLeft } from "lucide-react";

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

export default function SeckillDetail() {
  const params = useParams();
  const [, setLocation] = useLocation();
  const [activity, setActivity] = useState<SeckillActivity | null>(null);
  const [loading, setLoading] = useState(true);
  const [participating, setParticipating] = useState(false);
  const [seckillPath, setSeckillPath] = useState("");

  useEffect(() => {
    if (params.id) {
      fetchActivityDetail(params.id);
    }
  }, [params.id]);

  const fetchActivityDetail = async (id: string) => {
    try {
      const response = await fetch(`http://localhost:8080/product/seckill/${id}`);
      const result = await response.json();

      if (result.code === 200) {
        setActivity(result.data);
      } else {
        toast.error("Failed to load activity details");
      }
    } catch (error) {
      toast.error("Network error");
    } finally {
      setLoading(false);
    }
  };

  const handleGetSeckillPath = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      toast.error("Please login first");
      setLocation("/login");
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/seckill/path/${params.id}`, {
        headers: {
          "Authorization": `Bearer ${token}`,
        },
      });
      const result = await response.json();

      if (result.code === 200) {
        setSeckillPath(result.data);
        toast.success("Ready to participate!");
      } else {
        toast.error(result.message || "Failed to get seckill path");
      }
    } catch (error) {
      toast.error("Network error");
    }
  };

  const handleParticipate = async () => {
    if (!seckillPath) {
      toast.error("Please click 'Prepare to Participate' first");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      toast.error("Please login first");
      setLocation("/login");
      return;
    }

    setParticipating(true);

    try {
      const response = await fetch(`http://localhost:8080/seckill/doSeckill/${seckillPath}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ activityId: params.id }),
      });

      const result = await response.json();

      if (result.code === 200) {
        if (result.data.status === 1) {
          toast.success("Seckill successful!");
        } else if (result.data.status === 0) {
          toast.info("You are in the queue, please wait...");
          // Poll for result
          setTimeout(() => checkSeckillResult(), 2000);
        } else {
          toast.error(result.data.message || "Seckill failed");
        }
      } else {
        toast.error(result.message || "Failed to participate");
      }
    } catch (error) {
      toast.error("Network error");
    } finally {
      setParticipating(false);
    }
  };

  const checkSeckillResult = async () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
      const response = await fetch(`http://localhost:8080/seckill/result/${params.id}`, {
        headers: {
          "Authorization": `Bearer ${token}`,
        },
      });

      const result = await response.json();

      if (result.code === 200) {
        if (result.data.status === 1) {
          toast.success("Seckill successful!");
        } else if (result.data.status === 0) {
          toast.info("Still in queue...");
          setTimeout(() => checkSeckillResult(), 2000);
        } else {
          toast.error(result.data.message || "Seckill failed");
        }
      }
    } catch (error) {
      console.error("Error checking result:", error);
    }
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

  if (!activity) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="text-center py-12">
          <CardContent>
            <p className="text-gray-600">Activity not found</p>
            <Button className="mt-4" onClick={() => setLocation("/products")}>
              Back to List
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const now = new Date();
  const start = new Date(activity.startTime);
  const end = new Date(activity.endTime);
  const isActive = now >= start && now <= end;
  const hasEnded = now > end;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <Button variant="ghost" onClick={() => setLocation("/products")}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to List
          </Button>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8 max-w-4xl">
        <Card>
          <CardHeader>
            <div className="flex justify-between items-start">
              <div>
                <CardTitle className="text-3xl mb-2">{activity.productName}</CardTitle>
                <CardDescription>Seckill Activity #{activity.id}</CardDescription>
              </div>
              {hasEnded ? (
                <Badge variant="destructive">Ended</Badge>
              ) : isActive ? (
                <Badge variant="default" className="bg-green-500">Active Now</Badge>
              ) : (
                <Badge variant="secondary">Not Started</Badge>
              )}
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <Tag className="h-5 w-5 text-gray-500" />
                  <div>
                    <p className="text-sm text-gray-600">Seckill Price</p>
                    <p className="text-3xl font-bold text-red-600">¥{activity.seckillPrice}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Package className="h-5 w-5 text-gray-500" />
                  <div>
                    <p className="text-sm text-gray-600">Available Stock</p>
                    <p className="text-xl font-semibold">{activity.seckillStock} items</p>
                  </div>
                </div>
              </div>
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <Clock className="h-5 w-5 text-gray-500" />
                  <div>
                    <p className="text-sm text-gray-600">Start Time</p>
                    <p className="text-sm font-medium">{new Date(activity.startTime).toLocaleString()}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Clock className="h-5 w-5 text-gray-500" />
                  <div>
                    <p className="text-sm text-gray-600">End Time</p>
                    <p className="text-sm font-medium">{new Date(activity.endTime).toLocaleString()}</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="border-t pt-6 space-y-4">
              {!seckillPath && isActive && (
                <Button 
                  className="w-full" 
                  size="lg"
                  onClick={handleGetSeckillPath}
                >
                  Prepare to Participate
                </Button>
              )}
              {seckillPath && isActive && (
                <Button 
                  className="w-full bg-red-600 hover:bg-red-700" 
                  size="lg"
                  onClick={handleParticipate}
                  disabled={participating}
                >
                  {participating ? "Participating..." : "Participate Now!"}
                </Button>
              )}
              {hasEnded && (
                <Button className="w-full" size="lg" disabled>
                  Seckill Has Ended
                </Button>
              )}
              {!isActive && !hasEnded && (
                <Button className="w-full" size="lg" disabled>
                  Seckill Not Started Yet
                </Button>
              )}
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
