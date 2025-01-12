import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { useFonts } from 'expo-font';
import { Stack } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import 'react-native-reanimated';
import { Button, NativeModules, Platform } from 'react-native';
import * as Notifications from "expo-notifications";
const { MyServiceModule } = NativeModules;
import { useColorScheme } from './../hooks/useColorScheme';

// Prevent the splash screen from auto-hiding before asset loading is complete.

SplashScreen.preventAutoHideAsync();
// console.log(NativeModules);
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});
async function registerForPushNotificationsAsync() {
  let token;

  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;

  if (existingStatus !== "granted") {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }

  if (finalStatus !== "granted") {
    alert("You need to enable notifications in settings");
    return;
  }
  token = (await Notifications.getExpoPushTokenAsync()).data;
  console.log("Expo Push Token:", token);

  if (Platform.OS === "android") {
    await Notifications.setNotificationChannelAsync("default", {
      name: "default",
      importance: Notifications.AndroidImportance.MAX,
vibrationPattern: [0, 250, 250, 250],
      sound: true,
      lightColor: "#FF231F7C",
      bypassDnd: true,
    });
  }

  const responseListener =
    Notifications.addNotificationResponseReceivedListener((response) => {
      console.log("User interacted with notification:", response);
    });

  return () => {
    Notifications.removeNotificationSubscription(responseListener);
  };
}
const scheduleNotification = async (res:any) => {
  try {
    await Notifications.scheduleNotificationAsync({
      content: {
        title: "مرحبًا! - IVY STEM Platform",
        body: "هذه إشعار محلي.",
        data: { screen: "parentTrans", type: "parent" },
      },
      trigger: { seconds: 0 },
    });
    // await Notifications.scheduleNotificationAsync({
    //   content: {
    //     title: "مرحبًا! - IVY STEM Platform",
    //     body: "هذه إشعار محلي.",
    //     data: { screen: "smLMS", type: "staff" },
    //   },
    //   trigger: { seconds: 0 },
    // });
  } catch (error) {
    console.error("خطأ في جدولة الإشعار:", error);
  }
};
export default function RootLayout() {
  const colorScheme = useColorScheme();
  const [loaded] = useFonts({
    SpaceMono: require('../assets/fonts/SpaceMono-Regular.ttf'),
  });

  useEffect(() => {
    const backgroundListener =
      Notifications.addNotificationResponseReceivedListener(
        async (response) => {
          console.log(response);
          // const screen = response.notification.request.content.data.screen;
          // const type = response.notification.request.content.data.type;
          // type === "parent" &&
          //   (await AsyncStorage.setItem("lastNotificationRouteParent", screen));
          // type === "staff" &&
          //   (await AsyncStorage.setItem("lastNotificationRouteStaff", screen));
          // console.log("lastNotificationRoute Stored");
        }
      );
    if (loaded) {
      registerForPushNotificationsAsync()
      MyServiceModule?.startService();
      SplashScreen.hideAsync();
    }
    return ()=>{ 
      backgroundListener.remove();
      // MyServiceModule?.stopService();
    }
  }, [loaded]);

  if (!loaded) {
    return null;
  }

  return (
    <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
      <Stack>
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="+not-found" />
      </Stack>
      <Button title="xx" onPress={scheduleNotification} />
      <StatusBar style="auto" />
    </ThemeProvider>
  );
}
